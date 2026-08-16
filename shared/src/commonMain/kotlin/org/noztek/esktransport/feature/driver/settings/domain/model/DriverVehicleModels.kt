package org.noztek.esktransport.feature.driver.settings.domain.model

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord

data class DriverVehicle(
    val publicId: String,
    val vehicleTypeCode: String?,
    val plate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val payloadKg: Double?,
    val volumeM3: Double?,
    val passengerCapacity: Int?,
    val status: String,
    val isAvailable: Boolean,
    val isActiveRideVehicle: Boolean,
    val verificationStatus: DriverRequirementStatus,
    val documents: List<DriverVehicleDocument>,
    val services: List<DriverVehicleServiceStatus>,
)

data class DriverVehicleDocument(
    val type: String,
    val status: DriverRequirementStatus,
    val filePath: String?,
    val rejectionReason: String?,
    val reviewedAt: String?,
    val expiresAt: String?,
)

data class DriverVehicleServiceStatus(
    val serviceType: DriverVehicleServiceType,
    val status: DriverRequirementStatus,
    val isEnabled: Boolean,
    val rejectionReason: String?,
    val reviewedAt: String?,
)

data class DriverVehiclePayload(
    val vehicleTypeCode: String,
    val plate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val payloadKg: Double?,
    val volumeM3: Double?,
    val passengerCapacity: Int?,
    val services: List<DriverVehicleServiceType> = emptyList(),
)

data class DriverVehicleServicesPayload(
    val services: List<DriverVehicleServiceType>,
)

data class DriverVehicleDocumentUploadPayload(
    val type: DriverOnboardingDocumentType,
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val expiresAt: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DriverVehicleDocumentUploadPayload) return false

        return type == other.type &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes) &&
            expiresAt == other.expiresAt
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (expiresAt?.hashCode() ?: 0)
        return result
    }
}

data class DriverVehicleType(
    val code: String,
    val name: String,
    val description: String?,
    val supportsCargo: Boolean,
    val passengerMax: Int?,
    val sortOrder: Int,
    val allowedServices: List<DriverVehicleServiceType>,
)

val DriverVehicle.displayName: String
    get() = listOfNotNull(make, model)
        .joinToString(" ")
        .ifBlank { vehicleTypeCode ?: "Vehicle" }
        .replace('_', ' ')
        .uppercaseFirstLetterOfEachWord()

val DriverVehicle.vehiclePhotoDocument: DriverVehicleDocument?
    get() = documents.firstOrNull { it.type == DriverOnboardingDocumentType.VehiclePhoto.apiValue }

val DriverVehicle.enabledServiceLabels: List<String>
    get() = services
        .filter { it.isEnabled }
        .map { it.serviceType.displayName }

fun DriverVehicle.hasApprovedRideService(): Boolean {
    return services.any {
        it.serviceType == DriverVehicleServiceType.Ride &&
            it.isEnabled &&
            it.status == DriverRequirementStatus.Approved
    }
}
