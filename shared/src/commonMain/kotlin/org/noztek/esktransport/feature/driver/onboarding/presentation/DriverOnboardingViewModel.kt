package org.noztek.esktransport.feature.driver.onboarding.presentation

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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleSetupPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SaveDriverVehicleSetupUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverOnboardingUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UploadDriverOnboardingDocumentUseCase

class DriverOnboardingViewModel(
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val saveDriverVehicleSetupUseCase: SaveDriverVehicleSetupUseCase,
    private val uploadDriverOnboardingDocumentUseCase: UploadDriverOnboardingDocumentUseCase,
    private val submitDriverOnboardingUseCase: SubmitDriverOnboardingUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverOnboardingUiState())
    val uiState: StateFlow<DriverOnboardingUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) { getDriverOnboardingStatusUseCase() }
            result.fold(
                onSuccess = ::applyStatus,
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load driver setup.",
                        )
                    }
                },
            )
        }
    }

    fun updateLicenseNo(value: String) {
        _uiState.update { it.copy(licenseNo = value) }
    }

    fun updateLicenseExpiry(value: String) {
        _uiState.update { it.copy(licenseExpiry = value) }
    }

    fun updateVehicleType(value: String) {
        _uiState.update { it.copy(vehicleTypeCode = value) }
    }

    fun updatePlate(value: String) {
        _uiState.update { it.copy(plate = value.uppercase()) }
    }

    fun updateMake(value: String) {
        _uiState.update { it.copy(make = value) }
    }

    fun updateModel(value: String) {
        _uiState.update { it.copy(model = value) }
    }

    fun updateYear(value: String) {
        _uiState.update { it.copy(year = value.filter(Char::isDigit).take(4)) }
    }

    fun updatePassengerCapacity(value: String) {
        _uiState.update { it.copy(passengerCapacity = value.filter(Char::isDigit).take(2)) }
    }

    fun saveVehicle() {
        val state = _uiState.value
        if (state.plate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Plate number is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingVehicle = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) {
                saveDriverVehicleSetupUseCase(
                    DriverVehicleSetupPayload(
                        vehicleTypeCode = state.vehicleTypeCode,
                        plate = state.plate.trim(),
                        make = state.make.trim().ifBlank { null },
                        model = state.model.trim().ifBlank { null },
                        year = state.year.toIntOrNull(),
                        passengerCapacity = state.passengerCapacity.toIntOrNull(),
                    ),
                )
            }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "Vehicle setup saved.")
                    _uiState.update { it.copy(isSavingVehicle = false) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingVehicle = false,
                            errorMessage = throwable.message ?: "Unable to save vehicle.",
                        )
                    }
                },
            )
        }
    }

    fun uploadDocument(
        type: DriverOnboardingDocumentType,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    uploadingType = type,
                    errorMessage = null,
                    successMessage = null,
                    capturedPreviews = it.capturedPreviews + (
                        type to CapturedDocumentPreview(
                            fileName = fileName,
                            mimeType = mimeType,
                            bytes = bytes,
                        )
                        ),
                )
            }
            val result = withContext(ioDispatcher) {
                uploadDriverOnboardingDocumentUseCase(
                    DriverOnboardingDocumentUpload(
                        type = type,
                        fileName = fileName,
                        mimeType = mimeType,
                        bytes = bytes,
                        licenseNo = state.licenseNo.trim().ifBlank { null },
                        licenseExpiry = state.licenseExpiry.trim().ifBlank { null },
                    ),
                )
            }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "${type.displayName} uploaded.")
                    _uiState.update { it.copy(uploadingType = null) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            uploadingType = null,
                            errorMessage = throwable.message ?: "Unable to upload ${type.displayName.lowercase()}.",
                        )
                    }
                },
            )
        }
    }

    fun submitForReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) { submitDriverOnboardingUseCase() }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "Setup submitted for review.")
                    _uiState.update { it.copy(isSubmitting = false) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "Unable to submit setup.",
                        )
                    }
                },
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun applyStatus(
        status: DriverOnboardingStatus,
        successMessage: String? = null,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                status = status,
                errorMessage = null,
                successMessage = successMessage,
                licenseNo = status.license.licenseNo ?: it.licenseNo,
                licenseExpiry = status.license.licenseExpiry ?: it.licenseExpiry,
                vehicleTypeCode = status.vehicle.vehicleTypeCode ?: it.vehicleTypeCode,
                plate = status.vehicle.plate ?: it.plate,
                make = status.vehicle.make ?: it.make,
                model = status.vehicle.model ?: it.model,
                year = status.vehicle.year?.toString() ?: it.year,
                passengerCapacity = status.vehicle.passengerCapacity?.toString() ?: it.passengerCapacity,
            )
        }
    }
}
