package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingUseCase

class MarketplaceListingDetailsViewModel(
    private val getMarketplaceListingUseCase: GetMarketplaceListingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceListingDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun load(publicId: String) {
        if (publicId.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    listing = it.listing?.takeIf { listing -> listing.publicId == publicId },
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
}
