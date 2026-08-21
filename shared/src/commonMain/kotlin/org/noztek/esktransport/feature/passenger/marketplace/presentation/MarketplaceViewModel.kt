package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetRentalListingsUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetRentalVehicleTypesUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingPhotoUseCase

class MarketplaceViewModel(
    private val getRentalVehicleTypesUseCase: GetRentalVehicleTypesUseCase,
    private val getRentalListingsUseCase: GetRentalListingsUseCase,
    private val getMarketplaceListingPhotoUseCase: GetMarketplaceListingPhotoUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingVehicleTypes = true,
                    isLoadingListings = true,
                    errorMessage = null,
                )
            }

            val vehicleTypesResult = withContext(ioDispatcher) { getRentalVehicleTypesUseCase() }
            val vehicleTypes = vehicleTypesResult.getOrDefault(emptyList())
            val selectedCode = _uiState.value.selectedVehicleTypeCode
                ?.takeIf { code -> vehicleTypes.any { it.code == code } }

            _uiState.update {
                it.copy(
                    isLoadingVehicleTypes = false,
                    vehicleTypes = vehicleTypes,
                    selectedVehicleTypeCode = selectedCode,
                    errorMessage = vehicleTypesResult.exceptionOrNull()?.message,
                )
            }

            loadListings(selectedCode)
        }
    }

    fun selectVehicleType(code: String?) {
        if (_uiState.value.selectedVehicleTypeCode == code) return
        _uiState.update { it.copy(selectedVehicleTypeCode = code) }
        loadListings(code)
    }

    private fun loadListings(vehicleTypeCode: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingListings = true, errorMessage = null) }
            val listingsResult = withContext(ioDispatcher) { getRentalListingsUseCase(vehicleTypeCode) }

            _uiState.update {
                val listings = listingsResult.getOrDefault(emptyList())
                val listingPublicIds = listings.map { listing -> listing.publicId }.toSet()
                it.copy(
                    isLoadingListings = false,
                    listings = listings,
                    listingPhotoBytes = it.listingPhotoBytes
                        .filterKeys { publicId -> publicId in listingPublicIds },
                    loadingPhotoPublicIds = it.loadingPhotoPublicIds
                        .filter { publicId -> publicId in listingPublicIds }
                        .toSet(),
                    errorMessage = listingsResult.exceptionOrNull()?.message,
                )
            }
            listingsResult.getOrNull()?.let { listings ->
                loadListingPhotos(listings.map { it.publicId })
            }
        }
    }

    private fun loadListingPhotos(publicIds: List<String>) {
        publicIds
            .filterNot { publicId -> _uiState.value.listingPhotoBytes.containsKey(publicId) }
            .filterNot { publicId -> _uiState.value.loadingPhotoPublicIds.contains(publicId) }
            .forEach { publicId ->
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(loadingPhotoPublicIds = it.loadingPhotoPublicIds + publicId)
                    }
                    val result = withContext(ioDispatcher) { getMarketplaceListingPhotoUseCase(publicId) }
                    _uiState.update {
                        val bytes = result.getOrNull()
                        it.copy(
                            listingPhotoBytes = if (bytes != null) {
                                it.listingPhotoBytes + (publicId to bytes)
                            } else {
                                it.listingPhotoBytes
                            },
                            loadingPhotoPublicIds = it.loadingPhotoPublicIds - publicId,
                        )
                    }
                }
            }
    }
}
