package org.noztek.esktransport.feature.passenger.trip_tracking.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.dto.TripFeedbackRequestDto
import org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.dto.TripTrackingResponseDto

class TripTrackingApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTripTrackingSession(bookingPublicId: String): TripTrackingResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/bookings/$bookingPublicId/trip-session").body()
    }

    suspend fun cancelTrip(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/passenger/bookings/$bookingPublicId/cancel")
    }

    suspend fun submitFeedback(
        bookingPublicId: String,
        request: TripFeedbackRequestDto,
    ) {
        client.post("${baseUrl.trimEnd('/')}/api/passenger/bookings/$bookingPublicId/feedback") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
