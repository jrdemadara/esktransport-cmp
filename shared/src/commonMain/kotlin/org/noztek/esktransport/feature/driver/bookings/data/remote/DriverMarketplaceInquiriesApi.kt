package org.noztek.esktransport.feature.driver.bookings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.driver.bookings.data.remote.dto.DriverMarketplaceInquiriesResponseDto

class DriverMarketplaceInquiriesApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getInquiries(): DriverMarketplaceInquiriesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-inquiries").body()
    }
}
