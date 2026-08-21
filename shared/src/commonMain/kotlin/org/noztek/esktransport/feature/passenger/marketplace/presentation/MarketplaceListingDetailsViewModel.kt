package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingPhotoUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingUseCase

class MarketplaceListingDetailsViewModel(
    private val getMarketplaceListingUseCase: GetMarketplaceListingUseCase,
    private val getMarketplaceListingPhotoUseCase: GetMarketplaceListingPhotoUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceListingDetailsUiState())
    val uiState = _uiState.asStateFlow()

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
            val result = withContext(ioDispatcher) { getMarketplaceListingPhotoUseCase(listing.publicId) }
            result.getOrNull()?.let { bytes ->
                _uiState.update { it.copy(vehiclePhotoBytes = bytes) }
            }
        }
    }
}
