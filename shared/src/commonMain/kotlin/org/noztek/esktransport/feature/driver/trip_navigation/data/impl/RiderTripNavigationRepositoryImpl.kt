package org.noztek.esktransport.feature.rider.trip_navigation.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.RiderTripNavigationApi
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripLocationUpdateRequestDto
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPoint
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession
import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class RiderTripNavigationRepositoryImpl(
    private val api: RiderTripNavigationApi,
) : RiderTripNavigationRepository {
    override suspend fun getTripSession(bookingPublicId: String): Result<RiderTripSession> {
        return try {
            val response = api.getTripSession(bookingPublicId)
            val data = response.data
            val pickupLat = data.pickup.lat
            val pickupLng = data.pickup.lng
            val destinationLat = data.destination.lat
            val destinationLng = data.destination.lng

            if (pickupLat == null || pickupLng == null || destinationLat == null || destinationLng == null) {
                return Result.failure(IllegalStateException("Trip points are missing in trip session response."))
            }

            val phase = when (data.status) {
                "in_progress" -> RiderTripPhase.TO_DESTINATION
                else -> RiderTripPhase.TO_PICKUP
            }

            Result.success(
                RiderTripSession(
                    bookingPublicId = data.bookingPublicId,
                    phase = phase,
                    passengerName = data.passengerName,
                    pickupLabel = data.pickup.label ?: "Pickup",
                    destinationLabel = data.destination.label ?: "Destination",
                    pickupPoint = RiderTripPoint(latitude = pickupLat, longitude = pickupLng),
                    destinationPoint = RiderTripPoint(latitude = destinationLat, longitude = destinationLng),
                ),
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load trip session.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun arrivePickup(bookingPublicId: String): Result<Unit> {
        return try {
            api.arrivePickup(bookingPublicId)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to confirm pickup.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun updateTripLocation(
        bookingPublicId: String,
        latitude: Double,
        longitude: Double,
        bearing: Double?,
        speedKph: Double?,
        accuracyM: Double?,
        phase: String?,
    ): Result<Unit> {
        return try {
            api.updateTripLocation(
                bookingPublicId = bookingPublicId,
                request = RiderTripLocationUpdateRequestDto(
                    lat = latitude,
                    lng = longitude,
                    bearing = bearing,
                    speedKph = speedKph,
                    accuracyM = accuracyM,
                    recordedAt = null,
                    phase = phase,
                ),
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to update trip location.")
            Result.failure(IllegalStateException(message))
        }
    }
}
