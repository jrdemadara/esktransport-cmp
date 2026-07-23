package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverSettingsApi
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverSettingsRepository

class DriverSettingsRepositoryImpl(
    private val api: DriverSettingsApi,
) : DriverSettingsRepository {
    override suspend fun getProfilePhoto(): Result<ByteArray?> {
        return try {
            Result.success(api.getProfilePhoto())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load profile photo.")
            Result.failure(IllegalStateException(message))
        }
    }
}
