package org.noztek.esktransport.feature.rider.trip_navigation.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripSessionResponseDto
import org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto.RiderTripLocationUpdateRequestDto

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

    suspend fun arrivePickup(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/arrive-pickup")
    }

    suspend fun cancelBooking(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/cancel")
    }
}
