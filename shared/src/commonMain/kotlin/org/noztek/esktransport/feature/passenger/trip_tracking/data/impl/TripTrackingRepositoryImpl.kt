package org.noztek.esktransport.feature.passenger.trip_tracking.data.impl

import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.TripTrackingApi
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.LatestLocation
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.RiderTripInfo
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripPoint
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository.TripTrackingRepository

class TripTrackingRepositoryImpl(
    private val api: TripTrackingApi
) : TripTrackingRepository {
    override suspend fun getTripTrackingSession(bookingPublicId: String): TripTrackingSession {
        val dto = api.getTripTrackingSession(bookingPublicId).data
        return TripTrackingSession(
            bookingPublicId = dto.bookingPublicId,
            status = dto.status,
            pickupPoint = TripPoint(
                label = dto.pickup.label ?: "",
                latitude = dto.pickup.lat ?: 0.0,
                longitude = dto.pickup.lng ?: 0.0
            ),
            destinationPoint = TripPoint(
                label = dto.destination.label ?: "",
                latitude = dto.destination.lat ?: 0.0,
                longitude = dto.destination.lng ?: 0.0
            ),
            riderInfo = RiderTripInfo(
                publicId = dto.rider.publicId,
                name = dto.rider.name,
                vehicleType = dto.rider.vehicleTypeCode,
                vehicleLabel = dto.rider.vehicleLabel,
                vehiclePlate = dto.rider.vehiclePlate
            ),
            latestLocation = LatestLocation(
                latitude = dto.latestLocation.lat,
                longitude = dto.latestLocation.lng,
                bearing = dto.latestLocation.bearing,
                speedKph = dto.latestLocation.speedKph
            )
        )
    }
}
