package org.noztek.esktransport.feature.passenger.trip_tracking.domain.repository

import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession

interface TripTrackingRepository {
    suspend fun getTripTrackingSession(bookingPublicId: String): TripTrackingSession
    suspend fun cancelTrip(bookingPublicId: String): Result<Unit>
}
