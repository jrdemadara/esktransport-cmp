package org.noztek.esktransport.feature.passenger.trip_tracking.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
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
                latitude = dto.pickup.lat,
                longitude = dto.pickup.lng
            ),
            destinationPoint = TripPoint(
                label = dto.destination.label ?: "",
                latitude = dto.destination.lat,
                longitude = dto.destination.lng
            ),
            riderInfo = RiderTripInfo(
                publicId = dto.rider.publicId,
                name = dto.rider.name,
                rating = dto.rider.rating,
                vehicleType = dto.rider.vehicleTypeCode,
                vehicleLabel = dto.rider.vehicleLabel,
                vehiclePlate = dto.rider.vehiclePlate
            ),
            latestLocation = dto.latestLocation?.let { latestLocation ->
                LatestLocation(
                    latitude = latestLocation.lat,
                    longitude = latestLocation.lng,
                    bearing = latestLocation.bearing,
                    speedKph = latestLocation.speedKph
                )
            }
        )
    }

    override suspend fun cancelTrip(bookingPublicId: String): Result<Unit> {
        return try {
            api.cancelTrip(bookingPublicId)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Cancel trip failed.")
            Result.failure(IllegalStateException(message))
        }
    }
}
