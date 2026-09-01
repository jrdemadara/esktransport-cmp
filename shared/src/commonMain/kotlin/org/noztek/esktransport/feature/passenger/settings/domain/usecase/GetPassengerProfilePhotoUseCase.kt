package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.repository.PassengerSettingsRepository

class GetPassengerProfilePhotoUseCase(
    private val repository: PassengerSettingsRepository,
) {
    suspend operator fun invoke() = repository.getProfilePhoto()
}
