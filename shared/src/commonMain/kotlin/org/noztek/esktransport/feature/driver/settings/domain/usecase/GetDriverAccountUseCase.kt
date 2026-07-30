package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverSettingsRepository

class GetDriverAccountUseCase(
    private val repository: DriverSettingsRepository,
) {
    suspend operator fun invoke(): Result<DriverAccountProfile> = repository.getAccount()
}
