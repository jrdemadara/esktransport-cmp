package org.noztek.esktransport.feature.driver.home.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeStatsApi
import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeStatsRepository

class DriverHomeStatsRepositoryImpl(
    private val api: DriverHomeStatsApi,
) : DriverHomeStatsRepository {
    override suspend fun getStats(): Result<DriverHomeStats> {
        return try {
            Result.success(api.getStats().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load driver stats.")
            Result.failure(IllegalStateException(message))
        }
    }
}
