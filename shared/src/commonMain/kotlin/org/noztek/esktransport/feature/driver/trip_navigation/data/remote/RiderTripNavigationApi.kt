package org.noztek.esktransport.feature.rider.trip_navigation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripFeedbackRequestDto
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripLocationUpdateRequestDto
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripSessionResponseDto

class RiderTripNavigationApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getTripSession(bookingPublicId: String): RiderTripSessionResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/trip-session").body()
    }

    suspend fun updateTripLocation(
        bookingPublicId: String,
        request: RiderTripLocationUpdateRequestDto,
    ) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/location") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun confirmPickup(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/confirm-pickup")
    }

    suspend fun completeTrip(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/complete")
    }

    suspend fun submitFeedback(
        bookingPublicId: String,
        request: RiderTripFeedbackRequestDto,
    ) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/feedback") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun cancelBooking(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/cancel")
    }
}
