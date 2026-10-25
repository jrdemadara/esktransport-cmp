package org.noztek.esktransport.feature.driver.go.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.go.data.remote.GoApi
import org.noztek.esktransport.feature.driver.go.domain.repository.GoRepository
import org.noztek.esktransport.feature.driver.go.data.remote.dto.DriverAvailabilityLocationRequestDto
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import kotlin.time.Clock

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

    override suspend fun updateLocation(point: GeoPoint): Result<Unit> {
        return try {
            api.updateLocation(
                DriverAvailabilityLocationRequestDto(
                    lat = point.latitude,
                    lng = point.longitude,
                    recordedAt = Clock.System.now().toString(),
                ),
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update rider location.")
            Result.failure(IllegalStateException(message))
        }
    }
}
