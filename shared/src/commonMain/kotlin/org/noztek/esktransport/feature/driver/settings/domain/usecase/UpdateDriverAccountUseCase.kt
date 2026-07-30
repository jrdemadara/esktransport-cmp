package org.noztek.esktransport.feature.driver.settings.domain.usecase

import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverSettingsRepository

class UpdateDriverAccountUseCase(
    private val repository: DriverSettingsRepository,
) {
    suspend operator fun invoke(
        email: String?,
        address: String?,
    ): Result<DriverAccountProfile> {
        return repository.updateAccount(
            email = email?.trim()?.takeIf { it.isNotBlank() },
            address = address?.trim()?.takeIf { it.isNotBlank() },
        )
    }
}
