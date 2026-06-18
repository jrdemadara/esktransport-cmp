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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleRegistrationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleSetupPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SaveDriverVehicleSetupUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverIdentityVerificationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverOnboardingUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverVehicleRegistrationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UploadDriverOnboardingDocumentUseCase

class DriverOnboardingViewModel(
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val saveDriverVehicleSetupUseCase: SaveDriverVehicleSetupUseCase,
    private val submitDriverIdentityVerificationUseCase: SubmitDriverIdentityVerificationUseCase,
    private val submitDriverVehicleRegistrationUseCase: SubmitDriverVehicleRegistrationUseCase,
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

    fun captureDocumentPreview(
        type: DriverOnboardingDocumentType,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) {
        _uiState.update {
            it.copy(
                capturedPreviews = it.capturedPreviews + (
                    type to CapturedDocumentPreview(
                        fileName = fileName,
                        mimeType = mimeType,
                        bytes = bytes,
                    )
                    ),
            )
        }
    }

    fun submitIdentityVerification(onSuccess: () -> Unit) {
        val state = _uiState.value
        val licenseNo = state.licenseNo.trim()
        val licenseExpiry = state.licenseExpiry.trim()
        val licenseFront = state.capturedPreviews[DriverOnboardingDocumentType.LicenseFront]
        val licenseBack = state.capturedPreviews[DriverOnboardingDocumentType.LicenseBack]
        val selfie = state.capturedPreviews[DriverOnboardingDocumentType.Selfie]

        when {
            licenseNo.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "License number is required.") }
                return
            }
            licenseExpiry.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "License expiry is required.") }
                return
            }
            licenseFront == null -> {
                _uiState.update { it.copy(errorMessage = "License front capture is required.") }
                return
            }
            licenseBack == null -> {
                _uiState.update { it.copy(errorMessage = "License back capture is required.") }
                return
            }
            selfie == null -> {
                _uiState.update { it.copy(errorMessage = "Selfie capture is required.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingIdentity = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) {
                submitDriverIdentityVerificationUseCase(
                    DriverIdentityVerificationPayload(
                        licenseNo = licenseNo,
                        licenseExpiry = licenseExpiry,
                        licenseFront = licenseFront.toUpload(DriverOnboardingDocumentType.LicenseFront),
                        licenseBack = licenseBack.toUpload(DriverOnboardingDocumentType.LicenseBack),
                        selfie = selfie.toUpload(DriverOnboardingDocumentType.Selfie),
                    ),
                )
            }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "Identity verification submitted for review.")
                    _uiState.update { it.copy(isSubmittingIdentity = false) }
                    onSuccess()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmittingIdentity = false,
                            errorMessage = throwable.message ?: "Unable to submit identity verification.",
                        )
                    }
                },
            )
        }
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

    fun submitVehicleRegistration(onSuccess: () -> Unit) {
        val state = _uiState.value
        val registrationPreview = state.capturedPreviews[DriverOnboardingDocumentType.VehicleRegistration]

        when {
            state.plate.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Plate number is required.") }
                return
            }
            registrationPreview == null -> {
                _uiState.update { it.copy(errorMessage = "Vehicle registration capture is required.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingVehicleRegistration = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) {
                submitDriverVehicleRegistrationUseCase(
                    DriverVehicleRegistrationPayload(
                        vehicle = DriverVehicleSetupPayload(
                            vehicleTypeCode = state.vehicleTypeCode,
                            plate = state.plate.trim(),
                            make = state.make.trim().ifBlank { null },
                            model = state.model.trim().ifBlank { null },
                            year = state.year.toIntOrNull(),
                            passengerCapacity = state.passengerCapacity.toIntOrNull(),
                        ),
                        registrationDocument = registrationPreview.toUpload(DriverOnboardingDocumentType.VehicleRegistration),
                    ),
                )
            }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "Vehicle registration submitted for review.")
                    _uiState.update { it.copy(isSubmittingVehicleRegistration = false) }
                    onSuccess()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmittingVehicleRegistration = false,
                            errorMessage = throwable.message ?: "Unable to submit vehicle registration.",
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

private fun CapturedDocumentPreview.toUpload(type: DriverOnboardingDocumentType): DriverOnboardingDocumentUpload {
    return DriverOnboardingDocumentUpload(
        type = type,
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
    )
}
