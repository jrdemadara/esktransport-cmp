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
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehiclePayload
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType
import org.noztek.esktransport.feature.driver.settings.domain.usecase.AddDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehicleTypesUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverVehicleUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UploadDriverVehicleDocumentUseCase

data class DriverVehicleDocumentDraft(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DriverVehicleDocumentDraft) return false

        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class DriverVehicleFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val vehiclePublicId: String? = null,
    val vehicleTypeCode: String = "motorcycle",
    val plate: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val passengerCapacity: String = "",
    val payloadKg: String = "",
    val volumeM3: String = "",
    val vehicleTypes: List<DriverVehicleType> = emptyList(),
    val selectedServices: Set<DriverVehicleServiceType> = setOf(DriverVehicleServiceType.Ride),
    val documentDrafts: Map<DriverOnboardingDocumentType, DriverVehicleDocumentDraft> = emptyMap(),
    val errorMessage: String? = null,
)

class DriverVehicleFormViewModel(
    private val getDriverVehicleTypesUseCase: GetDriverVehicleTypesUseCase,
    private val getDriverVehicleUseCase: GetDriverVehicleUseCase,
    private val addDriverVehicleUseCase: AddDriverVehicleUseCase,
    private val updateDriverVehicleUseCase: UpdateDriverVehicleUseCase,
    private val uploadDriverVehicleDocumentUseCase: UploadDriverVehicleDocumentUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverVehicleFormUiState())
    val uiState: StateFlow<DriverVehicleFormUiState> = _uiState.asStateFlow()

    fun load(vehiclePublicId: String?) {
        if (_uiState.value.vehiclePublicId == vehiclePublicId && _uiState.value.vehicleTypes.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, vehiclePublicId = vehiclePublicId, errorMessage = null) }
            val vehicleTypesResult = withContext(ioDispatcher) { getDriverVehicleTypesUseCase() }
            vehicleTypesResult.fold(
                onSuccess = { vehicleTypes ->
                    if (vehiclePublicId == null) {
                        _uiState.update {
                            DriverVehicleFormUiState(
                                isLoading = false,
                                vehicleTypes = vehicleTypes,
                                vehicleTypeCode = vehicleTypes.defaultVehicleTypeCode(),
                                selectedServices = vehicleTypes.defaultSelectedServices(),
                            )
                        }
                        return@launch
                    }

                    val result = withContext(ioDispatcher) { getDriverVehicleUseCase(vehiclePublicId) }
                    result.fold(
                        onSuccess = { vehicle ->
                            _uiState.update { vehicle.toFormState(vehicleTypes) }
                        },
                        onFailure = { throwable ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    vehicleTypes = vehicleTypes,
                                    errorMessage = throwable.message ?: "Unable to load vehicle.",
                                )
                            }
                        },
                    )
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load vehicle types.",
                        )
                    }
                },
            )
        }
    }

    fun updateVehicleType(value: String) = update {
        val allowedServices = vehicleTypes
            .firstOrNull { it.code == value }
            ?.allowedServices
            .orEmpty()
            .toSet()
        val nextServices = selectedServices
            .filter { it in allowedServices }
            .toSet()
            .ifEmpty { allowedServices.defaultSelectedServices() }

        copy(
            vehicleTypeCode = value,
            selectedServices = nextServices,
        )
    }

    fun updatePlate(value: String) = update { copy(plate = value.uppercase()) }
    fun updateMake(value: String) = update { copy(make = value) }
    fun updateModel(value: String) = update { copy(model = value) }
    fun updateYear(value: String) = update { copy(year = value.filter(Char::isDigit).take(4)) }
    fun updatePassengerCapacity(value: String) = update { copy(passengerCapacity = value.filter(Char::isDigit).take(2)) }
    fun updatePayloadKg(value: String) = update { copy(payloadKg = value.decimalInput()) }
    fun updateVolumeM3(value: String) = update { copy(volumeM3 = value.decimalInput()) }
    fun setDocumentDraft(
        type: DriverOnboardingDocumentType,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) = update {
        copy(
            documentDrafts = documentDrafts + (
                type to DriverVehicleDocumentDraft(
                    fileName = fileName,
                    mimeType = mimeType,
                    bytes = bytes,
                )
                ),
        )
    }

    fun toggleService(serviceType: DriverVehicleServiceType) = update {
        if (serviceType !in allowedServicesForSelectedType()) return@update this

        val nextServices = if (serviceType in selectedServices) {
            selectedServices - serviceType
        } else {
            selectedServices + serviceType
        }
        copy(selectedServices = nextServices)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        val payload = state.toPayloadOrNull()
            ?: run {
                _uiState.update { it.copy(errorMessage = "Plate number is required.") }
                return
            }
        if (state.selectedServices.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Choose at least one vehicle use.") }
            return
        }

        if (state.vehiclePublicId == null && !state.hasRequiredNewVehicleDocuments()) {
            _uiState.update { it.copy(errorMessage = "Capture the registration document and vehicle photo.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = withContext(ioDispatcher) {
                state.vehiclePublicId?.let { vehiclePublicId ->
                    updateDriverVehicleUseCase(vehiclePublicId, payload)
                } ?: addDriverVehicleUseCase(payload)
            }
            result.fold(
                onSuccess = {
                    uploadDocumentsThenFinish(
                        vehicle = it,
                        drafts = state.documentDrafts,
                        onSuccess = onSuccess,
                    )
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save vehicle.",
                        )
                    }
                },
            )
        }
    }

    private suspend fun uploadDocumentsThenFinish(
        vehicle: DriverVehicle,
        drafts: Map<DriverOnboardingDocumentType, DriverVehicleDocumentDraft>,
        onSuccess: () -> Unit,
    ) {
        for ((type, draft) in drafts) {
            val result = withContext(ioDispatcher) {
                uploadDriverVehicleDocumentUseCase(
                    vehicle.publicId,
                    DriverVehicleDocumentUploadPayload(
                        type = type,
                        fileName = draft.fileName,
                        mimeType = draft.mimeType,
                        bytes = draft.bytes,
                    ),
                )
            }

            result.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Vehicle saved, but document upload failed.",
                    )
                }
                return
            }
        }

        _uiState.update { it.copy(isSaving = false) }
        onSuccess()
    }

    private fun update(block: DriverVehicleFormUiState.() -> DriverVehicleFormUiState) {
        _uiState.update { it.block().copy(errorMessage = null) }
    }
}

private fun DriverVehicle.toFormState(vehicleTypes: List<DriverVehicleType>): DriverVehicleFormUiState {
    val selectedTypeCode = vehicleTypeCode ?: vehicleTypes.defaultVehicleTypeCode()
    val allowedServices = vehicleTypes.firstOrNull { it.code == selectedTypeCode }
        ?.allowedServices
        .orEmpty()
        .toSet()
    val enabledServices = services
        .filter { it.isEnabled }
        .map { it.serviceType }
        .filter { it in allowedServices }
        .toSet()
        .ifEmpty { allowedServices.defaultSelectedServices() }

    return DriverVehicleFormUiState(
        isLoading = false,
        vehiclePublicId = publicId,
        vehicleTypeCode = selectedTypeCode,
        plate = plate,
        make = make.orEmpty(),
        model = model.orEmpty(),
        year = year?.toString().orEmpty(),
        passengerCapacity = passengerCapacity?.toString().orEmpty(),
        payloadKg = payloadKg?.cleanNumber().orEmpty(),
        volumeM3 = volumeM3?.cleanNumber().orEmpty(),
        vehicleTypes = vehicleTypes,
        selectedServices = enabledServices,
        documentDrafts = emptyMap(),
    )
}

private fun DriverVehicleFormUiState.toPayloadOrNull(): DriverVehiclePayload? {
    val normalizedPlate = plate.trim().uppercase()
    if (normalizedPlate.isBlank()) return null

    return DriverVehiclePayload(
        vehicleTypeCode = vehicleTypeCode,
        plate = normalizedPlate,
        make = make.trim().ifBlank { null },
        model = model.trim().ifBlank { null },
        year = year.toIntOrNull(),
        payloadKg = payloadKg.toDoubleOrNull(),
        volumeM3 = volumeM3.toDoubleOrNull(),
        passengerCapacity = passengerCapacity.toIntOrNull(),
        services = selectedServices.toList(),
    )
}

private fun String.decimalInput(): String {
    val filtered = filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')

    return if (firstDot == -1) {
        filtered.take(8)
    } else {
        filtered.take(firstDot + 1) + filtered.drop(firstDot + 1).replace(".", "").take(2)
    }
}

private fun Double.cleanNumber(): String {
    return if (this % 1.0 == 0.0) toInt().toString() else toString()
}

private fun List<DriverVehicleType>.defaultVehicleTypeCode(): String {
    return firstOrNull { DriverVehicleServiceType.Ride in it.allowedServices }?.code
        ?: firstOrNull()?.code
        ?: "motorcycle"
}

private fun List<DriverVehicleType>.defaultSelectedServices(): Set<DriverVehicleServiceType> {
    return firstOrNull { it.code == defaultVehicleTypeCode() }
        ?.allowedServices
        ?.toSet()
        ?.defaultSelectedServices()
        ?: setOf(DriverVehicleServiceType.Ride)
}

private fun Set<DriverVehicleServiceType>.defaultSelectedServices(): Set<DriverVehicleServiceType> {
    return when {
        DriverVehicleServiceType.Ride in this -> setOf(DriverVehicleServiceType.Ride)
        isNotEmpty() -> setOf(first())
        else -> setOf(DriverVehicleServiceType.Ride)
    }
}

private fun DriverVehicleFormUiState.allowedServicesForSelectedType(): Set<DriverVehicleServiceType> {
    return vehicleTypes
        .firstOrNull { it.code == vehicleTypeCode }
        ?.allowedServices
        .orEmpty()
        .toSet()
}

private fun DriverVehicleFormUiState.hasRequiredNewVehicleDocuments(): Boolean {
    return DriverOnboardingDocumentType.VehicleRegistration in documentDrafts &&
        DriverOnboardingDocumentType.VehiclePhoto in documentDrafts
}
