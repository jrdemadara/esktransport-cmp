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

class MarketplaceViewModel(
    private val getRentalVehicleTypesUseCase: GetRentalVehicleTypesUseCase,
    private val getRentalListingsUseCase: GetRentalListingsUseCase,
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
                it.copy(
                    isLoadingListings = false,
                    listings = listingsResult.getOrDefault(emptyList()),
                    errorMessage = listingsResult.exceptionOrNull()?.message,
                )
            }
        }
    }
}
