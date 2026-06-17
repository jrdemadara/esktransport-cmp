package org.noztek.esktransport.feature.driver.go.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.go.data.remote.GoApi
import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository

class GoRepositoryImpl(
    private val api: GoApi,
) : GoRepository {
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
