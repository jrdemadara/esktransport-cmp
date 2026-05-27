package org.noztek.esktransport.feature.passenger.trip_tracking.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.dto.TripTrackingResponseDto

class TripTrackingApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTripTrackingSession(bookingPublicId: String): TripTrackingResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/bookings/$bookingPublicId/trip-session").body()
    }
}
