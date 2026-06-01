package org.noztek.esktransport.feature.rider.trip_navigation.domain.repository

import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession

interface RiderTripNavigationRepository {
    suspend fun getTripSession(bookingPublicId: String): Result<RiderTripSession>
    suspend fun arrivePickup(bookingPublicId: String): Result<Unit>
    suspend fun updateTripLocation(
        bookingPublicId: String,
        latitude: Double,
        longitude: Double,
        bearing: Double?,
        speedKph: Double?,
        accuracyM: Double?,
        phase: String?,
    ): Result<Unit>
}
