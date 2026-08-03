package org.noztek.esktransport.feature.driver.onboarding.domain.repository

import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZone
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverServiceZoneSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceSelectionPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleSetupPayload

interface DriverOnboardingRepository {
    suspend fun getStatus(): Result<DriverOnboardingStatus>
    suspend fun submitIdentityVerification(payload: DriverIdentityVerificationPayload): Result<DriverOnboardingStatus>
    suspend fun saveVehicle(payload: DriverVehicleSetupPayload): Result<DriverOnboardingStatus>
    suspend fun getServiceZones(): Result<List<DriverServiceZone>>
    suspend fun submitServiceZones(payload: DriverServiceZoneSelectionPayload): Result<DriverOnboardingStatus>
    suspend fun submitVehicleServices(payload: DriverVehicleServiceSelectionPayload): Result<DriverOnboardingStatus>
    suspend fun uploadDocument(upload: DriverOnboardingDocumentUpload): Result<DriverOnboardingStatus>
}
