package org.noztek.esktransport.feature.driver.settings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.settings.data.remote.DriverLocationSharingApi
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.toRequestDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettings
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverLocationSharingSettingsPayload
import org.noztek.esktransport.feature.driver.settings.domain.repository.DriverLocationSharingRepository

class DriverLocationSharingRepositoryImpl(
    private val api: DriverLocationSharingApi,
) : DriverLocationSharingRepository {
    override suspend fun getSettings(): Result<DriverLocationSharingSettings> {
        return try {
            Result.success(api.getSettings().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load location sharing settings.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateSettings(
        payload: DriverLocationSharingSettingsPayload,
    ): Result<DriverLocationSharingSettings> {
        return try {
            Result.success(api.updateSettings(payload.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update location sharing settings.")
            Result.failure(IllegalStateException(message))
        }
    }
}
