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
import org.noztek.esktransport.core.realtime.model.displayMessage
import org.noztek.esktransport.core.realtime.model.matchesDriver
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZoneSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleRegistrationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleSetupPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverServiceZonesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.ObserveDriverOnboardingStatusChangedUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverIdentityVerificationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverServiceZonesUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubmitDriverVehicleRegistrationUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.SubscribeDriverOnboardingRealtimeUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.UnsubscribeDriverOnboardingRealtimeUseCase

class DriverOnboardingViewModel(
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val getDriverServiceZonesUseCase: GetDriverServiceZonesUseCase,
    private val submitDriverIdentityVerificationUseCase: SubmitDriverIdentityVerificationUseCase,
    private val submitDriverVehicleRegistrationUseCase: SubmitDriverVehicleRegistrationUseCase,
    private val submitDriverServiceZonesUseCase: SubmitDriverServiceZonesUseCase,
    private val observeDriverOnboardingStatusChangedUseCase: ObserveDriverOnboardingStatusChangedUseCase,
    private val subscribeDriverOnboardingRealtimeUseCase: SubscribeDriverOnboardingRealtimeUseCase,
    private val unsubscribeDriverOnboardingRealtimeUseCase: UnsubscribeDriverOnboardingRealtimeUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverOnboardingUiState())
    val uiState: StateFlow<DriverOnboardingUiState> = _uiState.asStateFlow()

    init {
        observeDriverOnboardingRealtime()
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

    fun updateAddress(value: String) {
        _uiState.update { it.copy(address = value) }
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

    fun loadServiceZones() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingServiceZones = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) { getDriverServiceZonesUseCase() }
            result.fold(
                onSuccess = { zones ->
                    _uiState.update {
                        it.copy(
                            isLoadingServiceZones = false,
                            serviceZones = zones,
                            selectedServiceZoneIds = if (it.selectedServiceZoneIds.isEmpty()) {
                                it.status?.serviceZones?.map { zone -> zone.id }?.toSet().orEmpty()
                            } else {
                                it.selectedServiceZoneIds
                            },
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingServiceZones = false,
                            errorMessage = throwable.message ?: "Unable to load service zones.",
                        )
                    }
                },
            )
        }
    }

    fun toggleServiceZone(zoneId: Long) {
        _uiState.update {
            val selected = it.selectedServiceZoneIds
            it.copy(
                selectedServiceZoneIds = if (zoneId in selected) {
                    selected - zoneId
                } else {
                    selected + zoneId
                },
            )
        }
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
        val address = state.address.trim()
        val licenseExpiry = state.licenseExpiry.trim()
        val licenseFront = state.capturedPreviews[DriverOnboardingDocumentType.LicenseFront]
        val licenseBack = state.capturedPreviews[DriverOnboardingDocumentType.LicenseBack]
        val selfie = state.capturedPreviews[DriverOnboardingDocumentType.Selfie]

        when {
            licenseNo.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "License number is required.") }
                return
            }
            address.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Address is required.") }
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
                        address = address,
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

    fun submitVehicleRegistration(onSuccess: () -> Unit) {
        val state = _uiState.value
        val registrationPreview = state.capturedPreviews[DriverOnboardingDocumentType.VehicleRegistration]
        val vehiclePhotoPreview = state.capturedPreviews[DriverOnboardingDocumentType.VehiclePhoto]

        when {
            state.plate.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Plate number is required.") }
                return
            }
            registrationPreview == null -> {
                _uiState.update { it.copy(errorMessage = "Vehicle registration capture is required.") }
                return
            }
            vehiclePhotoPreview == null -> {
                _uiState.update { it.copy(errorMessage = "Vehicle photo capture is required.") }
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
                        vehiclePhoto = vehiclePhotoPreview.toUpload(DriverOnboardingDocumentType.VehiclePhoto),
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

    fun submitServiceZones(onSuccess: () -> Unit) {
        val state = _uiState.value
        val zoneIds = state.selectedServiceZoneIds.toList()

        if (zoneIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Choose at least one service zone.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingServiceZones = true, errorMessage = null, successMessage = null) }
            val result = withContext(ioDispatcher) {
                submitDriverServiceZonesUseCase(
                    DriverServiceZoneSelectionPayload(serviceZoneIds = zoneIds),
                )
            }
            result.fold(
                onSuccess = { status ->
                    applyStatus(status, successMessage = "Service zones saved.")
                    _uiState.update { it.copy(isSubmittingServiceZones = false) }
                    onSuccess()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmittingServiceZones = false,
                            errorMessage = throwable.message ?: "Unable to save service zones.",
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
        subscribeDriverOnboardingRealtimeUseCase(status.driverId)
        _uiState.update {
            it.copy(
                isLoading = false,
                status = status,
                errorMessage = null,
                successMessage = successMessage,
                licenseNo = status.license.licenseNo ?: it.licenseNo,
                address = status.address ?: it.address,
                licenseExpiry = status.license.licenseExpiry ?: it.licenseExpiry,
                vehicleTypeCode = status.vehicle.vehicleTypeCode ?: it.vehicleTypeCode,
                plate = status.vehicle.plate ?: it.plate,
                make = status.vehicle.make ?: it.make,
                model = status.vehicle.model ?: it.model,
                year = status.vehicle.year?.toString() ?: it.year,
                passengerCapacity = status.vehicle.passengerCapacity?.toString() ?: it.passengerCapacity,
                selectedServiceZoneIds = status.serviceZones.map { zone -> zone.id }.toSet(),
            )
        }
    }

    override fun onCleared() {
        unsubscribeDriverOnboardingRealtimeUseCase()
        super.onCleared()
    }

    private fun observeDriverOnboardingRealtime() {
        viewModelScope.launch {
            observeDriverOnboardingStatusChangedUseCase().collect { event ->
                val currentDriverId = _uiState.value.status?.driverId
                if (event.matchesDriver(currentDriverId)) {
                    refreshFromRealtime(event.displayMessage())
                }
            }
        }
    }

    private fun refreshFromRealtime(message: String?) {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) { getDriverOnboardingStatusUseCase() }
            result.fold(
                onSuccess = { status ->
                    applyStatus(
                        status = status,
                        successMessage = message ?: "Your driver setup status has been updated.",
                    )
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Unable to refresh driver setup.")
                    }
                },
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
