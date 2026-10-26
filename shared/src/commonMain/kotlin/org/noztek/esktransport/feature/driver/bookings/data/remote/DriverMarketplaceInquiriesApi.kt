package org.noztek.esktransport.feature.driver.bookings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.bookings.data.remote.dto.DriverMarketplaceInquiriesResponseDto
import org.noztek.esktransport.feature.driver.bookings.data.remote.dto.ReviewMarketplaceRequestDto

class DriverMarketplaceInquiriesApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getInquiries(): DriverMarketplaceInquiriesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-inquiries").body()
    }

    suspend fun acceptRequest(requestPublicId: String, quotedAmount: Double, currency: String = "PHP", ownerNote: String? = null) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-service-requests/$requestPublicId/accept") {
            contentType(ContentType.Application.Json)
            setBody(ReviewMarketplaceRequestDto(quotedAmount, currency, ownerNote))
        }
    }

    suspend fun startRequest(requestPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-service-requests/$requestPublicId/start")
    }

    suspend fun completeRequest(requestPublicId: String) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-service-requests/$requestPublicId/complete")
    }
}
