package org.noztek.esktransport.feature.common.active_booking.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.common.active_booking.data.remote.dto.ActiveBookingResponseDto

class ActiveBookingApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getPassengerActiveBooking(): ActiveBookingResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/bookings/active").body()
    }

    suspend fun getDriverActiveBooking(): ActiveBookingResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/active").body()
    }
}
