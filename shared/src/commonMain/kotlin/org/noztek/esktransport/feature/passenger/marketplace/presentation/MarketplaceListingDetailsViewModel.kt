package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.CreateMarketplaceServiceRequestPayload
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingPhotoUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.SendMarketplaceVehicleRequestUseCase

sealed class MarketplaceListingDetailsUiEvent {
    data class ShowSnackbar(val message: String) : MarketplaceListingDetailsUiEvent()
}

class MarketplaceListingDetailsViewModel(
    private val getMarketplaceListingUseCase: GetMarketplaceListingUseCase,
    private val getMarketplaceListingPhotoUseCase: GetMarketplaceListingPhotoUseCase,
    private val sendMarketplaceVehicleRequestUseCase: SendMarketplaceVehicleRequestUseCase,
    private val marketplaceRepository: MarketplaceRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceListingDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<MarketplaceListingDetailsUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<MarketplaceListingDetailsUiEvent> = _uiEvents.asSharedFlow()

    fun load(publicId: String) {
        if (publicId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                val currentListing = it.listing?.takeIf { listing -> listing.publicId == publicId }
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    listing = currentListing,
                    vehiclePhotoBytes = it.vehiclePhotoBytes.takeIf { currentListing != null },
                    isVehiclePhotoLoading = false,
                    requestedStartAt = it.requestedStartAt.takeIf { currentListing != null }.orEmpty(),
                    requestedEndAt = it.requestedEndAt.takeIf { currentListing != null }.orEmpty(),
                    pickupLabel = it.pickupLabel.takeIf { currentListing != null }.orEmpty(),
                    destinationLabel = it.destinationLabel.takeIf { currentListing != null }.orEmpty(),
                    passengerCount = it.passengerCount.takeIf { currentListing != null }.orEmpty(),
                    cargoDescription = it.cargoDescription.takeIf { currentListing != null }.orEmpty(),
                    cargoWeightKg = it.cargoWeightKg.takeIf { currentListing != null }.orEmpty(),
                    cargoPieces = it.cargoPieces.takeIf { currentListing != null }.orEmpty(),
                    customerNote = it.customerNote.takeIf { currentListing != null }.orEmpty(),
                    isSendingRequest = false,
                    sentRequestPublicId = it.sentRequestPublicId.takeIf { currentListing != null },
                )
            }

            val result = withContext(ioDispatcher) { getMarketplaceListingUseCase(publicId) }
            result
                .onSuccess { listing ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            listing = listing,
                            errorMessage = null,
                        )
                    }
                    loadListingPhoto(listing)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load listing.",
                        )
                    }
                }
        }
    }

    private fun loadListingPhoto(listing: MarketplaceListing) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVehiclePhotoLoading = true) }
            val result = withContext(ioDispatcher) { getMarketplaceListingPhotoUseCase(listing.publicId) }
            _uiState.update {
                it.copy(
                    vehiclePhotoBytes = result.getOrNull(),
                    isVehiclePhotoLoading = false,
                )
            }
        }
    }

    fun onRequestedStartAtChange(value: String) {
        _uiState.update { it.copy(requestedStartAt = value.take(MaxRequestTextLength), errorMessage = null) }
    }

    fun onRequestedEndAtChange(value: String) {
        _uiState.update { it.copy(requestedEndAt = value.take(MaxRequestTextLength), errorMessage = null) }
    }

    fun onPickupLabelChange(value: String) {
        _uiState.update { it.copy(pickupLabel = value.take(MaxRequestTextLength), errorMessage = null) }
    }

    fun onDestinationLabelChange(value: String) {
        _uiState.update { it.copy(destinationLabel = value.take(MaxRequestTextLength), errorMessage = null) }
    }

    fun onPassengerCountChange(value: String) {
        _uiState.update {
            it.copy(
                passengerCount = value.filter(Char::isDigit).take(2),
                errorMessage = null,
            )
        }
    }

    fun onCargoDescriptionChange(value: String) {
        _uiState.update { it.copy(cargoDescription = value.take(MaxLongRequestTextLength), errorMessage = null) }
    }

    fun onCargoWeightKgChange(value: String) {
        _uiState.update { it.copy(cargoWeightKg = value.filterNumericInput().take(8), errorMessage = null) }
    }

    fun onCargoPiecesChange(value: String) {
        _uiState.update { it.copy(cargoPieces = value.filter(Char::isDigit).take(6), errorMessage = null) }
    }

    fun onCustomerNoteChange(value: String) {
        _uiState.update { it.copy(customerNote = value.take(MaxLongRequestTextLength), errorMessage = null) }
    }

    fun sendRequest() {
        val state = _uiState.value
        val listing = state.listing ?: return
        if (state.isSendingRequest || state.sentRequestPublicId != null) return

        val validationError = state.validateRequest()
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            _uiEvents.tryEmit(MarketplaceListingDetailsUiEvent.ShowSnackbar(validationError))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingRequest = true, errorMessage = null) }
            val result = withContext(ioDispatcher) {
                sendMarketplaceVehicleRequestUseCase(
                    CreateMarketplaceServiceRequestPayload(
                        listingPublicId = listing.publicId,
                        requestedStartAt = state.requestedStartAt.trimOrNull(),
                        requestedEndAt = state.requestedEndAt.trimOrNull(),
                        pickupLabel = state.pickupLabel.trimOrNull(),
                        destinationLabel = state.destinationLabel.trimOrNull(),
                        passengerCount = state.passengerCount.toIntOrNull(),
                        cargoDescription = state.cargoDescription.trimOrNull(),
                        cargoWeightKg = state.cargoWeightKg.toDoubleOrNull(),
                        cargoPieces = state.cargoPieces.toIntOrNull(),
                        customerNote = state.customerNote.trimOrNull(),
                    ),
                )
            }
            result
                .onSuccess { request ->
                    _uiState.update {
                        it.copy(
                            isSendingRequest = false,
                            sentRequestPublicId = request.publicId,
                            serviceRequest = request,
                            errorMessage = null,
                        )
                    }
                    _uiEvents.tryEmit(MarketplaceListingDetailsUiEvent.ShowSnackbar("Request sent for owner review."))
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Failed to send request."
                    _uiState.update {
                        it.copy(isSendingRequest = false, errorMessage = error)
                    }
                    _uiEvents.tryEmit(MarketplaceListingDetailsUiEvent.ShowSnackbar(error))
                }
        }
    }

    fun confirmRequest() = updateRequest { id -> marketplaceRepository.confirmRequest(id) }
    fun cancelRequest() = updateRequest { id -> marketplaceRepository.cancelRequest(id) }
    fun completeRequest(cashAmount: Double) = updateRequest { id -> marketplaceRepository.completeRequest(id, cashAmount, _uiState.value.serviceRequest?.currency ?: "PHP") }

    private fun updateRequest(action: suspend (String) -> Result<org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest>) {
        val id = _uiState.value.sentRequestPublicId ?: return
        if (_uiState.value.isUpdatingRequest) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingRequest = true, errorMessage = null) }
            withContext(ioDispatcher) { action(id) }
                .onSuccess { request -> _uiState.update { it.copy(serviceRequest = request, isUpdatingRequest = false) } }
                .onFailure { error -> _uiState.update { it.copy(isUpdatingRequest = false, errorMessage = error.message) } }
        }
    }
}

private const val MaxRequestTextLength = 255
private const val MaxLongRequestTextLength = 2000

private fun MarketplaceListingDetailsUiState.validateRequest(): String? {
    if (requestedStartAt.isBlank()) return "Select the requested start date."
    if (requestedEndAt.isBlank()) return "Select the requested end date."
    if (pickupLabel.isBlank()) return "Add the pickup location."
    if (destinationLabel.isBlank()) return "Add the destination."
    return null
}

private fun String.trimOrNull(): String? {
    return trim().takeIf { it.isNotBlank() }
}

private fun String.filterNumericInput(): String {
    var dotUsed = false
    return buildString(length) {
        this@filterNumericInput.forEach { char ->
            when {
                char.isDigit() -> append(char)
                char == '.' && !dotUsed -> {
                    append(char)
                    dotUsed = true
                }
            }
        }
    }
}
