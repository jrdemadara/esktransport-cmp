package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettingsPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverLocationSharingRepository

class UpdateDriverLocationSharingUseCase(
    private val repository: DriverLocationSharingRepository,
) {
    suspend operator fun invoke(payload: DriverLocationSharingSettingsPayload): Result<DriverLocationSharingSettings> {
        return repository.updateSettings(payload)
    }
}
