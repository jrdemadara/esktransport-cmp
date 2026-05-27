package org.noztek.esktransport.feature.passenger.ride_planner.data.impl

import org.noztek.esktransport.core.map.MapboxDirectionsClient
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.ride_planner.data.remote.RidePlannerApi
import org.noztek.esktransport.feature.passenger.ride_planner.data.remote.dto.RidePlannerRequestDto
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.NearbyDriver
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.Point
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailability
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.RideAvailabilityPayload
import org.noztek.esktransport.feature.passenger.ride_planner.domain.model.VehicleOption
import org.noztek.esktransport.feature.passenger.ride_planner.domain.repository.RidePlannerRepository

class RidePlannerRepositoryImpl(
    private val api: RidePlannerApi,
    private val mapboxDirectionsClient: MapboxDirectionsClient,
) : RidePlannerRepository {
    override suspend fun getNearbyDrivers(payload: RideAvailabilityPayload): Result<RideAvailability> {
        return runCatching {
            val response = api.getAvailability(
                RidePlannerRequestDto(
                    pickupLat = payload.pickupLat,
                    pickupLng = payload.pickupLng,
                    destinationLat = payload.destinationLat,
                    destinationLng = payload.destinationLng,
                    radiusKm = payload.radiusKm,
                    vehicleType = payload.vehicleType,
                ),
            )
            RideAvailability(
                pickup = Point(response.data.pickup.lat, response.data.pickup.lng),
                radiusKm = response.data.radiusKm,
                nearbyDriversCount = response.data.nearbyDriversCount,
                vehicleOptions = response.data.vehicleOptions.map {
                    VehicleOption(
                        vehicleTypeCode = it.vehicleTypeCode,
                        availableDrivers = it.availableDrivers,
                        minEtaMinutes = it.minEtaMinutes,
                        minDistanceM = it.minDistanceM,
                    )
                },
                nearbyDrivers = response.data.nearbyDrivers.map {
                    NearbyDriver(
                        driverPublicId = it.driverPublicId,
                        vehicleLabel = it.vehicleLabel,
                        vehiclePlate = it.vehiclePlate,
                        vehicleTypeCode = it.vehicleTypeCode,
                        lat = it.lat,
                        lng = it.lng,
                        distanceM = it.distanceM,
                        etaMinutes = it.etaMinutes,
                        heading = it.heading,
                        passengerCapacity = it.passengerCapacity,
                        rating = it.rating,
                        estimatedFare = it.estimatedFare,
                        currency = it.currency,
                    )
                },
            )
        }
    }

    override suspend fun getRoute(origin: GeoPoint, destination: GeoPoint): Result<List<GeoPoint>> {
        return mapboxDirectionsClient.getRoutePoints(
            originLongitude = origin.longitude,
            originLatitude = origin.latitude,
            destinationLongitude = destination.longitude,
            destinationLatitude = destination.latitude,
        ).map { points -> points.map { GeoPoint(latitude = it.latitude, longitude = it.longitude) } }
    }
}
