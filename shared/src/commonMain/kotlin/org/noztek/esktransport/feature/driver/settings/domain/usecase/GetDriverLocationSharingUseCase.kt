package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverLocationSharingRepository

class GetDriverLocationSharingUseCase(
    private val repository: DriverLocationSharingRepository,
) {
    suspend operator fun invoke(): Result<DriverLocationSharingSettings> = repository.getSettings()
}
