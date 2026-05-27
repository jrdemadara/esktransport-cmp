package org.noztek.esktransport.feature.driver.home.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeApi
import org.noztek.esktransport.feature.driver.home.domain.repository.DriverHomeRepository

class DriverHomeRepositoryImpl(
    private val api: DriverHomeApi,
) : DriverHomeRepository {
    override suspend fun getAvailability(): Result<Boolean> {
        return try {
            val response = api.getAvailability()
            Result.success(response.data.isAvailable)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to fetch rider availability.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun setAvailability(isAvailable: Boolean): Result<Boolean> {
        return try {
            val response = api.setAvailability(isAvailable)
            Result.success(response.data.isAvailable)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update rider availability.")
            Result.failure(IllegalStateException(message))
        }
    }
}
