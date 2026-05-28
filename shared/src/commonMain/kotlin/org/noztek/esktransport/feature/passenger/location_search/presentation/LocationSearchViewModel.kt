package org.noztek.esktransport.feature.passenger.location_search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.location_search.domain.model.PlaceSuggestion
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.GetCurrentLocationUseCase
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.ResolveTapLabelUseCase
import org.noztek.esktransport.feature.passenger.location_search.domain.usecase.SearchPlacesUseCase

class LocationSearchViewModel(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val resolveTapLabelUseCase: ResolveTapLabelUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var searchJob: Job? = null
    private var resolveMapIdleJob: Job? = null
    private var screenOpenedAtMs: Long = 0L

    private val _state = MutableStateFlow(LocationSearchUiState())
    val state: StateFlow<LocationSearchUiState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<LocationSearchUiEvent>()
    val events: SharedFlow<LocationSearchUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val currentLocation = getCurrentLocationUseCase()
            _state.update { it.copy(currentLocationPoint = currentLocation) }
            if (currentLocation != null) {
                _events.emit(LocationSearchUiEvent.MoveCamera(point = currentLocation, zoom = 15.0, animated = false))
            }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch(ioDispatcher) {
            if (query.isBlank()) {
                _state.update { it.copy(suggestions = emptyList()) }
                return@launch
            }
            delay(250)
            val suggestions = searchPlacesUseCase(
                query = query,
                location = _state.value.currentLocationPoint,
            )
            _state.update { it.copy(suggestions = suggestions) }
        }
    }

    fun onScreenOpened() {
        searchJob?.cancel()
        resolveMapIdleJob?.cancel()
        screenOpenedAtMs = Clock.System.now().toEpochMilliseconds()
        _state.update {
            it.copy(
                query = "",
                suggestions = emptyList(),
                tappedLocationLabel = null,
                selectedPoint = null,
                isMapMoving = false,
            )
        }
    }

    fun onSuggestionSelected(suggestion: PlaceSuggestion) {
        viewModelScope.launch(ioDispatcher) {
            _state.update {
                it.copy(
                    selectedPoint = suggestion.point,
                    tappedLocationLabel = suggestion.label,
                    isMapMoving = false,
                )
            }
            _events.emit(LocationSearchUiEvent.MoveCamera(point = suggestion.point, zoom = 15.5, animated = true))
        }
    }

    fun onMapTapped(point: GeoPoint) {
        onMapSettled(point)
    }

    fun onMapMoving(point: GeoPoint) {
        if (isWithinStartupWindow()) return
        resolveMapIdleJob?.cancel()
        _state.update {
            it.copy(
                selectedPoint = point,
                isMapMoving = true,
            )
        }
    }

    fun onMapSettled(point: GeoPoint) {
        if (isWithinStartupWindow()) return
        resolveMapIdleJob?.cancel()
        resolveMapIdleJob = viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(selectedPoint = point, isMapMoving = false) }
            delay(80)
            if (_state.value.selectedPoint == point && !_state.value.isMapMoving) {
                val label = resolveTapLabelUseCase(point)
                _state.update {
                    it.copy(
                        selectedPoint = point,
                        tappedLocationLabel = label,
                    )
                }
            }
        }
    }

    private fun isWithinStartupWindow(): Boolean {
        val elapsed = Clock.System.now().toEpochMilliseconds() - screenOpenedAtMs
        return elapsed in 0..900
    }

}
