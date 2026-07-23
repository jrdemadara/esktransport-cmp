package org.noztek.esktransport.feature.driver.trips.domain.repository

import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsDashboard

interface DriverTripsRepository {
    suspend fun getTrips(): Result<DriverTripsDashboard>
}
