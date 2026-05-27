package org.noztek.esktransport.feature.driver.home.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.home.data.remote.dto.DriverAvailabilityRequestDto
import org.noztek.esktransport.feature.driver.home.data.remote.dto.DriverAvailabilityResponseDto

class DriverHomeApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getAvailability(): DriverAvailabilityResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/availability").body()
    }

    suspend fun setAvailability(isAvailable: Boolean): DriverAvailabilityResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/availability") {
            contentType(ContentType.Application.Json)
            setBody(DriverAvailabilityRequestDto(isAvailable = isAvailable))
        }.body()
    }

    suspend fun acceptBookingOffer(bookingPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/bookings/$bookingPublicId/accept")
    }
}
