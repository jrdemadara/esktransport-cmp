package org.noztek.esktransport.feature.passenger.booking_review.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CancelBookingResponseDto
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CreateFareQuoteRequestDto
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CreateFareQuoteResponseDto
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CreateBookingRequestDto
import org.noztek.esktransport.feature.passenger.booking_review.data.remote.dto.CreateBookingResponseDto

class BookingReviewApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun createFareQuote(request: CreateFareQuoteRequestDto): CreateFareQuoteResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/fare-quotes") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun createBooking(request: CreateBookingRequestDto): CreateBookingResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/bookings") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun cancelBooking(bookingPublicId: String): CancelBookingResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/bookings/$bookingPublicId/cancel") {
            contentType(ContentType.Application.Json)
        }.body()
    }
}
