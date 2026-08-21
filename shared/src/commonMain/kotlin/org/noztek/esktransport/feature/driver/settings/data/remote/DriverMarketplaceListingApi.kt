package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverMarketplaceListingRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverMarketplaceListingResponseDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverMarketplaceListingsResponseDto

class DriverMarketplaceListingApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getListings(): DriverMarketplaceListingsResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-listings").body()
    }

    suspend fun updateListing(
        listingPublicId: String,
        request: DriverMarketplaceListingRequestDto,
    ): DriverMarketplaceListingResponseDto {
        return client.patch("${baseUrl.trimEnd('/')}/api/v1/rider/vehicle-listings/$listingPublicId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
