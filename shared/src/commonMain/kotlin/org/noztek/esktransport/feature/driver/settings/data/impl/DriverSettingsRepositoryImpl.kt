package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverSettingsApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.UpdateDriverAccountRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverSettingsRepository

class DriverSettingsRepositoryImpl(
    private val api: DriverSettingsApi,
) : DriverSettingsRepository {
    override suspend fun getAccount(): Result<DriverAccountProfile> {
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
    ): Result<DriverAccountProfile> {
        return try {
            val response = api.updateAccount(
                UpdateDriverAccountRequestDto(
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
}
