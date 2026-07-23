package org.noztek.esktransport.feature.driver.trips.domain.usecase

import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsDashboard
import org.noztek.esktransport.feature.driver.trips.domain.repository.DriverTripsRepository

class GetDriverTripsUseCase(
    private val repository: DriverTripsRepository,
) {
    suspend operator fun invoke(): Result<DriverTripsDashboard> = repository.getTrips()
}
