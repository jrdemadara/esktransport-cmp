package org.noztek.esktransport.feature.passenger.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlacePayload
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlaceType
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.GetCurrentLocationUseCase
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.ResolveTapLabelUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.CreateSavedPlaceUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.DeleteSavedPlaceUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.GetSavedPlacesUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.UpdateSavedPlaceUseCase

sealed class SavedPlacesUiEvent {
    data class ShowSnackbar(val message: String) : SavedPlacesUiEvent()
    data class FillPinnedAddress(val point: GeoPoint, val address: String) : SavedPlacesUiEvent()
    data object CloseEditor : SavedPlacesUiEvent()
}

class SavedPlacesViewModel(
    private val getSavedPlacesUseCase: GetSavedPlacesUseCase,
    private val createSavedPlaceUseCase: CreateSavedPlaceUseCase,
    private val updateSavedPlaceUseCase: UpdateSavedPlaceUseCase,
    private val deleteSavedPlaceUseCase: DeleteSavedPlaceUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val resolveTapLabelUseCase: ResolveTapLabelUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var resolvePinnedAddressJob: Job? = null

    private val _uiState = MutableStateFlow(SavedPlacesUiState())
    val uiState: StateFlow<SavedPlacesUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<SavedPlacesUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<SavedPlacesUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadCurrentLocation()
        refresh()
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch(ioDispatcher) {
            val point = runCatching { getCurrentLocationUseCase() }.getOrNull()
            if (point != null) {
                _uiState.value = _uiState.value.copy(currentLocationPoint = point)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getSavedPlacesUseCase()
                .onSuccess { places ->
                    _uiState.value = SavedPlacesUiState(
                        isLoading = false,
                        places = places.sortedWith(
                            compareBy({ it.sortOrder }, { it.label.lowercase() }),
                        ),
                        currentLocationPoint = _uiState.value.currentLocationPoint,
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load saved places.",
                    )
                }
        }
    }

    fun savePlace(existingPlace: SavedPlace?, form: SavedPlaceFormState) {
        val payload = form.toPayloadOrNull() ?: run {
            _uiEvents.tryEmit(SavedPlacesUiEvent.ShowSnackbar("Complete the saved place details."))
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            val result = if (existingPlace == null) {
                createSavedPlaceUseCase(payload)
            } else {
                updateSavedPlaceUseCase(id = existingPlace.id, payload = payload)
            }

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _uiEvents.tryEmit(SavedPlacesUiEvent.CloseEditor)
                    _uiEvents.tryEmit(SavedPlacesUiEvent.ShowSnackbar("Saved place updated."))
                    refresh()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _uiEvents.tryEmit(
                        SavedPlacesUiEvent.ShowSnackbar(error.message ?: "Failed to save place."),
                    )
                }
        }
    }

    fun onPinLocationSettled(point: GeoPoint) {
        resolvePinnedAddressJob?.cancel()
        resolvePinnedAddressJob = viewModelScope.launch(ioDispatcher) {
            delay(120)
            val label = resolveTapLabelUseCase(point).toSavedPlaceAddress()
            _uiEvents.tryEmit(SavedPlacesUiEvent.FillPinnedAddress(point = point, address = label))
        }
    }

    fun deletePlace(place: SavedPlace) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            deleteSavedPlaceUseCase(place.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _uiEvents.tryEmit(SavedPlacesUiEvent.CloseEditor)
                    _uiEvents.tryEmit(SavedPlacesUiEvent.ShowSnackbar("Saved place deleted."))
                    refresh()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    _uiEvents.tryEmit(
                        SavedPlacesUiEvent.ShowSnackbar(error.message ?: "Failed to delete saved place."),
                    )
                }
        }
    }
}

private fun String.toSavedPlaceAddress(): String {
    val parts = split(",").map { it.trim() }.filter { it.isNotBlank() }
    if (parts.isEmpty()) return this

    val filtered = parts.filterNot { it.isCityProvinceOrCountryPart() }
    return filtered.take(3).joinToString(", ").ifBlank {
        parts.take(2).joinToString(", ")
    }
}

private fun String.isCityProvinceOrCountryPart(): Boolean {
    val value = lowercase()
    return value == "philippines" ||
        value == "sultan kudarat" ||
        value == "south cotabato" ||
        value == "north cotabato" ||
        value == "cotabato" ||
        value == "soccsksargen" ||
        value == "region xii" ||
        value == "tacurong" ||
        value == "city of tacurong" ||
        value == "tacurong city" ||
        value == "koronadal" ||
        value == "koronadal city" ||
        value == "city of koronadal" ||
        value == "isulan" ||
        value.endsWith(" province") ||
        value.endsWith(" city") ||
        value.startsWith("city of ")
}

data class SavedPlaceFormState(
    val placeType: SavedPlaceType = SavedPlaceType.Custom,
    val label: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
) {
    fun toPayloadOrNull(): SavedPlacePayload? {
        val nextLatitude = latitude.toDoubleOrNull()
        val nextLongitude = longitude.toDoubleOrNull()
        if (label.isBlank() || address.isBlank() || nextLatitude == null || nextLongitude == null) {
            return null
        }

        return SavedPlacePayload(
            placeType = placeType,
            label = label.trim(),
            address = address.trim(),
            latitude = nextLatitude,
            longitude = nextLongitude,
        )
    }
}

fun SavedPlace.toFormState(): SavedPlaceFormState {
    return SavedPlaceFormState(
        placeType = placeType,
        label = label,
        address = address,
        latitude = latitude?.toString().orEmpty(),
        longitude = longitude?.toString().orEmpty(),
    )
}
