package org.noztek.esktransport.feature.passenger.settings.domain.repository

import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerAccountProfile
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerProfilePhotoUpload

interface PassengerSettingsRepository {
    suspend fun getAccount(): Result<PassengerAccountProfile>

    suspend fun updateAccount(
        email: String?,
        address: String?,
    ): Result<PassengerAccountProfile>

    suspend fun getProfilePhoto(): Result<ByteArray?>

    suspend fun uploadProfilePhoto(upload: PassengerProfilePhotoUpload): Result<PassengerAccountProfile>
}
