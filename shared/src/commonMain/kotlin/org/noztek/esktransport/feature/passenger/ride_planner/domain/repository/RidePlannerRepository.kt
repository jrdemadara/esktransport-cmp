package org.noztek.esktransport.feature.passenger.ride_planner.domain.repository

import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailability
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailabilityPayload

interface RidePlannerRepository {
    suspend fun getNearbyDrivers(payload: RideAvailabilityPayload): Result<RideAvailability>
    suspend fun getRoute(origin: GeoPoint, destination: GeoPoint): Result<List<GeoPoint>>
}
