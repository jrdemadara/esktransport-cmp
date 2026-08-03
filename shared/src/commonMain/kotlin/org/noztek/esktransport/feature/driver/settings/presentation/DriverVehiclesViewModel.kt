package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.usecase.ActivateDriverRideVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehiclesUseCase

data class DriverVehiclesUiState(
    val isLoading: Boolean = true,
    val isActivatingVehicleId: String? = null,
    val vehicles: List<DriverVehicle> = emptyList(),
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

class DriverVehiclesViewModel(
    private val getDriverVehiclesUseCase: GetDriverVehiclesUseCase,
    private val activateDriverRideVehicleUseCase: ActivateDriverRideVehicleUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverVehiclesUiState())
    val uiState: StateFlow<DriverVehiclesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(showLoading: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverVehiclesUseCase() }
            result.fold(
                onSuccess = { vehicles ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vehicles = vehicles,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load vehicles.",
                        )
                    }
                },
            )
        }
    }

    fun activateRideVehicle(vehicle: DriverVehicle) {
        if (vehicle.isActiveRideVehicle || _uiState.value.isActivatingVehicleId != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActivatingVehicleId = vehicle.publicId,
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) { activateDriverRideVehicleUseCase(vehicle.publicId) }
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isActivatingVehicleId = null,
                            statusMessage = "Active City Ride vehicle updated.",
                        )
                    }
                    refresh(showLoading = false)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isActivatingVehicleId = null,
                            errorMessage = throwable.message ?: "Unable to set active City Ride vehicle.",
                        )
                    }
                },
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
