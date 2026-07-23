package org.noztek.esktransport.feature.driver.trips.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.trips.data.remote.DriverTripsApi
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsDashboard
import org.noztek.esktransport.feature.driver.trips.domain.repository.DriverTripsRepository

class DriverTripsRepositoryImpl(
    private val api: DriverTripsApi,
) : DriverTripsRepository {
    override suspend fun getTrips(): Result<DriverTripsDashboard> {
        return try {
            Result.success(api.getTrips().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load trips.")
            Result.failure(IllegalStateException(message))
        }
    }
}
