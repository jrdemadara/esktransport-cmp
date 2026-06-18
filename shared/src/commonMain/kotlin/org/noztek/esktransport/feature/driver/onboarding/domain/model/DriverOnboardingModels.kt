package org.noztek.esktransport.feature.driver.onboarding.domain.model

data class DriverOnboardingStatus(
    val driverId: Long,
    val driverStatus: String,
    val status: DriverOnboardingState,
    val canGo: Boolean,
    val blockingReasons: List<String>,
    val stepStatuses: DriverStepStatuses,
    val license: DriverLicenseInfo,
    val vehicle: DriverVehicleInfo,
    val requirements: List<DriverOnboardingRequirement>,
)

data class DriverLicenseInfo(
    val licenseNo: String?,
    val licenseExpiry: String?,
    val identityVerificationStatus: DriverRequirementStatus,
    val identityRejectionReason: String?,
    val identityReviewedAt: String?,
)

data class DriverStepStatuses(
    val accountRegistration: DriverRequirementStatus,
    val identityVerification: DriverRequirementStatus,
    val vehicleRegistration: DriverRequirementStatus,
    val serviceRadius: DriverRequirementStatus,
)

data class DriverVehicleInfo(
    val exists: Boolean,
    val vehicleId: Long? = null,
    val vehicleTypeCode: String? = null,
    val plate: String? = null,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val passengerCapacity: Int? = null,
    val status: String? = null,
    val isAvailable: Boolean? = null,
)

data class DriverOnboardingRequirement(
    val type: DriverOnboardingDocumentType,
    val label: String,
    val status: DriverRequirementStatus,
    val filePath: String?,
    val rejectionReason: String?,
    val reviewedAt: String?,
    val expiresAt: String?,
)

data class DriverVehicleSetupPayload(
    val vehicleTypeCode: String,
    val plate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val passengerCapacity: Int?,
)

data class DriverVehicleRegistrationPayload(
    val vehicle: DriverVehicleSetupPayload,
    val registrationDocument: DriverOnboardingDocumentUpload,
    val vehiclePhoto: DriverOnboardingDocumentUpload,
)

data class DriverOnboardingDocumentUpload(
    val type: DriverOnboardingDocumentType,
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val licenseNo: String? = null,
    val licenseExpiry: String? = null,
    val expiresAt: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DriverOnboardingDocumentUpload) return false

        return type == other.type &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes) &&
            licenseNo == other.licenseNo &&
            licenseExpiry == other.licenseExpiry &&
            expiresAt == other.expiresAt
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (licenseNo?.hashCode() ?: 0)
        result = 31 * result + (licenseExpiry?.hashCode() ?: 0)
        result = 31 * result + (expiresAt?.hashCode() ?: 0)
        return result
    }
}

data class DriverIdentityVerificationPayload(
    val licenseNo: String,
    val licenseExpiry: String,
    val licenseFront: DriverOnboardingDocumentUpload,
    val licenseBack: DriverOnboardingDocumentUpload,
    val selfie: DriverOnboardingDocumentUpload,
)

enum class DriverOnboardingState {
    Incomplete,
    PendingReview,
    Rejected,
    Ready,
    Blocked,
}

enum class DriverRequirementStatus {
    Missing,
    Uploaded,
    PendingReview,
    Approved,
    Rejected,
    Expired,
}

enum class DriverOnboardingDocumentType(val apiValue: String, val displayName: String) {
    LicenseFront("driver_license_front", "License front"),
    LicenseBack("driver_license_back", "License back"),
    Selfie("driver_selfie", "Driver selfie"),
    VehicleRegistration("vehicle_registration", "Vehicle registration"),
    VehiclePhoto("vehicle_photo", "Vehicle photo"),
}

fun String.toDriverOnboardingState(): DriverOnboardingState {
    return when (lowercase()) {
        "ready" -> DriverOnboardingState.Ready
        "pending_review" -> DriverOnboardingState.PendingReview
        "rejected" -> DriverOnboardingState.Rejected
        "blocked" -> DriverOnboardingState.Blocked
        else -> DriverOnboardingState.Incomplete
    }
}

fun String.toDriverRequirementStatus(): DriverRequirementStatus {
    return when (lowercase()) {
        "uploaded" -> DriverRequirementStatus.Uploaded
        "pending_review" -> DriverRequirementStatus.PendingReview
        "approved" -> DriverRequirementStatus.Approved
        "rejected" -> DriverRequirementStatus.Rejected
        "expired" -> DriverRequirementStatus.Expired
        else -> DriverRequirementStatus.Missing
    }
}

fun String.toDriverOnboardingDocumentType(): DriverOnboardingDocumentType {
    return DriverOnboardingDocumentType.entries.firstOrNull { it.apiValue == this }
        ?: DriverOnboardingDocumentType.LicenseFront
}
