package org.noztek.esktransport.feature.passenger.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.settings.data.remote.PassengerSettingsApi
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.UpdatePassengerAccountRequestDto
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerAccountProfile
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerProfilePhotoUpload
import org.noztek.esktransport.feature.passenger.settings.domain.repository.PassengerSettingsRepository

class PassengerSettingsRepositoryImpl(
    private val api: PassengerSettingsApi,
) : PassengerSettingsRepository {
    override suspend fun getAccount(): Result<PassengerAccountProfile> {
        return try {
            Result.success(api.getAccount().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load account details.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateAccount(
        email: String?,
        address: String?,
    ): Result<PassengerAccountProfile> {
        return try {
            val response = api.updateAccount(
                UpdatePassengerAccountRequestDto(
                    email = email,
                    address = address,
                ),
            )
            Result.success(response.data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update account details.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getProfilePhoto(): Result<ByteArray?> {
        return try {
            Result.success(api.getProfilePhoto())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load profile photo.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun uploadProfilePhoto(upload: PassengerProfilePhotoUpload): Result<PassengerAccountProfile> {
        return try {
            Result.success(api.uploadProfilePhoto(upload).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update profile photo.")
            Result.failure(IllegalStateException(message))
        }
    }
}
