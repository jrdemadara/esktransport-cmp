package org.noztek.esktransport.feature.passenger.settings.domain.usecase

import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerProfilePhotoUpload
import org.noztek.esktransport.feature.passenger.settings.domain.repository.PassengerSettingsRepository

class UploadPassengerProfilePhotoUseCase(
    private val repository: PassengerSettingsRepository,
) {
    suspend operator fun invoke(upload: PassengerProfilePhotoUpload) = repository.uploadProfilePhoto(upload)
}
