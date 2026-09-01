package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.repository.PassengerSettingsRepository

class UpdatePassengerAccountUseCase(
    private val repository: PassengerSettingsRepository,
) {
    suspend operator fun invoke(
        email: String?,
        address: String?,
    ) = repository.updateAccount(
        email = email,
        address = address,
    )
}
