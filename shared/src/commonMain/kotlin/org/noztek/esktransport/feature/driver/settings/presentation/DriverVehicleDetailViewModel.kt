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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UploadDriverVehicleDocumentUseCase

data class DriverVehicleDetailUiState(
    val isLoading: Boolean = true,
    val isUploadingDocumentType: DriverOnboardingDocumentType? = null,
    val vehicle: DriverVehicle? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

class DriverVehicleDetailViewModel(
    private val getDriverVehicleUseCase: GetDriverVehicleUseCase,
    private val uploadDriverVehicleDocumentUseCase: UploadDriverVehicleDocumentUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverVehicleDetailUiState())
    val uiState: StateFlow<DriverVehicleDetailUiState> = _uiState.asStateFlow()

    private var currentVehiclePublicId: String? = null

    fun load(vehiclePublicId: String?) {
        if (vehiclePublicId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Vehicle not found.",
                )
            }
            return
        }
        if (currentVehiclePublicId == vehiclePublicId && _uiState.value.vehicle != null) return

        currentVehiclePublicId = vehiclePublicId
        refresh(showLoading = true)
    }

    fun refresh(showLoading: Boolean = false) {
        val vehiclePublicId = currentVehiclePublicId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = showLoading, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverVehicleUseCase(vehiclePublicId) }
            result.fold(
                onSuccess = { vehicle ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vehicle = vehicle,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load vehicle.",
                        )
                    }
                },
            )
        }
    }

    fun uploadCapturedDocument(
        type: DriverOnboardingDocumentType,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) {
        val vehiclePublicId = currentVehiclePublicId ?: return
        if (_uiState.value.isUploadingDocumentType != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingDocumentType = type,
                    errorMessage = null,
                    statusMessage = null,
                )
            }
            val result = withContext(ioDispatcher) {
                uploadDriverVehicleDocumentUseCase(
                    vehiclePublicId,
                    DriverVehicleDocumentUploadPayload(
                        type = type,
                        fileName = fileName,
                        mimeType = mimeType,
                        bytes = bytes,
                    ),
                )
            }
            result.fold(
                onSuccess = { vehicle ->
                    _uiState.update {
                        it.copy(
                            isUploadingDocumentType = null,
                            vehicle = vehicle,
                            statusMessage = "${type.shortUploadLabel()} uploaded for review.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isUploadingDocumentType = null,
                            errorMessage = throwable.message ?: "Upload failed. Try again.",
                        )
                    }
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, statusMessage = null) }
    }
}

private fun DriverOnboardingDocumentType.shortUploadLabel(): String {
    return when (this) {
        DriverOnboardingDocumentType.VehicleRegistration -> "Registration document"
        DriverOnboardingDocumentType.VehiclePhoto -> "Vehicle photo"
        else -> displayName
    }
}
