package org.noztek.esktransport.feature.driver.onboarding.data.impl

import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverOnboardingDataDto
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverLicenseInfo
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingRequirement
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverStepStatuses
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleInfo
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.toDriverRequirementStatus

internal fun DriverOnboardingDataDto.toDomain(): DriverOnboardingStatus {
    return DriverOnboardingStatus(
        driverId = driverId,
        driverStatus = driverStatus,
        status = status.toDriverOnboardingState(),
        canGo = canGo,
        blockingReasons = blockingReasons,
        stepStatuses = DriverStepStatuses(
            accountRegistration = stepStatuses.accountRegistration.toDriverRequirementStatus(),
            identityVerification = stepStatuses.identityVerification.toDriverRequirementStatus(),
            vehicleRegistration = stepStatuses.vehicleRegistration.toDriverRequirementStatus(),
            serviceRadius = stepStatuses.serviceRadius.toDriverRequirementStatus(),
        ),
        license = DriverLicenseInfo(
            licenseNo = license.licenseNo,
            licenseExpiry = license.licenseExpiry,
            identityVerificationStatus = license.identityVerificationStatus.toDriverRequirementStatus(),
            identityRejectionReason = license.identityRejectionReason,
            identityReviewedAt = license.identityReviewedAt,
        ),
        vehicle = DriverVehicleInfo(
            exists = vehicle.exists,
            vehicleId = vehicle.vehicleId,
            vehicleTypeCode = vehicle.vehicleTypeCode,
            plate = vehicle.plate,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            passengerCapacity = vehicle.passengerCapacity,
            status = vehicle.status,
            isAvailable = vehicle.isAvailable,
        ),
        requirements = requirements.map { requirement ->
            DriverOnboardingRequirement(
                type = requirement.type.toDriverOnboardingDocumentType(),
                label = requirement.label,
                status = requirement.status.toDriverRequirementStatus(),
                filePath = requirement.filePath,
                rejectionReason = requirement.rejectionReason,
                reviewedAt = requirement.reviewedAt,
                expiresAt = requirement.expiresAt,
            )
        },
    )
}
