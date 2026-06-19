package org.noztek.esktransport.feature.driver.onboarding.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverOnboardingResponseDto(
    val data: DriverOnboardingDataDto,
)

@Serializable
data class DriverOnboardingMutationResponseDto(
    val message: String,
    val data: DriverOnboardingDataDto,
)

@Serializable
data class DriverOnboardingDataDto(
    @SerialName("driver_id")
    val driverId: Long,
    @SerialName("driver_status")
    val driverStatus: String,
    val status: String,
    @SerialName("can_go")
    val canGo: Boolean,
    @SerialName("blocking_reasons")
    val blockingReasons: List<String> = emptyList(),
    @SerialName("step_statuses")
    val stepStatuses: DriverStepStatusesDto = DriverStepStatusesDto(),
    val license: DriverLicenseDto,
    val vehicle: DriverVehicleDto,
    @SerialName("service_zones")
    val serviceZones: List<DriverServiceZoneDto> = emptyList(),
    val requirements: List<DriverOnboardingRequirementDto> = emptyList(),
)

@Serializable
data class DriverLicenseDto(
    @SerialName("license_no")
    val licenseNo: String? = null,
    @SerialName("license_expiry")
    val licenseExpiry: String? = null,
    @SerialName("identity_verification_status")
    val identityVerificationStatus: String = "missing",
    @SerialName("identity_rejection_reason")
    val identityRejectionReason: String? = null,
    @SerialName("identity_reviewed_at")
    val identityReviewedAt: String? = null,
)

@Serializable
data class DriverStepStatusesDto(
    @SerialName("account_registration")
    val accountRegistration: String = "approved",
    @SerialName("identity_verification")
    val identityVerification: String = "missing",
    @SerialName("vehicle_registration")
    val vehicleRegistration: String = "missing",
    @SerialName("service_radius")
    val serviceRadius: String = "missing",
)

@Serializable
data class DriverVehicleDto(
    val exists: Boolean,
    @SerialName("vehicle_id")
    val vehicleId: Long? = null,
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String? = null,
    val plate: String? = null,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int? = null,
    val status: String? = null,
    @SerialName("is_available")
    val isAvailable: Boolean? = null,
)

@Serializable
data class DriverOnboardingRequirementDto(
    val type: String,
    val label: String,
    val status: String,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("reviewed_at")
    val reviewedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
)

@Serializable
data class DriverVehicleSetupRequestDto(
    @SerialName("vehicle_type_code")
    val vehicleTypeCode: String,
    val plate: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    @SerialName("passenger_capacity")
    val passengerCapacity: Int? = null,
)

@Serializable
data class DriverServiceZonesResponseDto(
    val data: List<DriverServiceZoneDto> = emptyList(),
)

@Serializable
data class DriverServiceZoneDto(
    val id: Long,
    val name: String,
    @SerialName("zone_type")
    val zoneType: String,
)

@Serializable
data class DriverServiceZoneSelectionRequestDto(
    @SerialName("service_zone_ids")
    val serviceZoneIds: List<Long>,
)
