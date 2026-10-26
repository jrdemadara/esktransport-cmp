package org.noztek.esktransport.feature.passenger.marketplace.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.CreateMarketplaceServiceRequestDto
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.CompleteMarketplaceRequestDto
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.MarketplaceListingsResponseDto
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.MarketplaceServiceRequestResponseDto
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.VehicleTypeLookupResponseDto

class MarketplaceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getRentalVehicleTypes(): VehicleTypeLookupResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/lookups/vehicle-types") {
            parameter("service_type", "rental")
        }.body()
    }

    suspend fun getRentalListings(
        vehicleTypeCode: String?,
    ): MarketplaceListingsResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings") {
            parameter("service_type", "rental")
            vehicleTypeCode?.let { parameter("vehicle_type_code", it) }
        }.body()
    }

    suspend fun getListing(publicId: String): MarketplaceListingsResponseDto.Single {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$publicId").body()
    }

    suspend fun sendRequest(
        publicId: String,
        request: CreateMarketplaceServiceRequestDto,
    ): MarketplaceServiceRequestResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$publicId/requests") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getRequest(publicId: String): MarketplaceServiceRequestResponseDto =
        client.get("${baseUrl.trimEnd('/')}/api/passenger/vehicle-service-requests/$publicId").body()

    suspend fun confirmRequest(publicId: String): MarketplaceServiceRequestResponseDto =
        client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-service-requests/$publicId/confirm").body()

    suspend fun cancelRequest(publicId: String): MarketplaceServiceRequestResponseDto =
        client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-service-requests/$publicId/cancel").body()

    suspend fun completeRequest(publicId: String, cashAmount: Double, currency: String): MarketplaceServiceRequestResponseDto =
        client.post("${baseUrl.trimEnd('/')}/api/passenger/vehicle-service-requests/$publicId/complete") {
            contentType(ContentType.Application.Json)
            setBody(CompleteMarketplaceRequestDto(cashAmount, currency))
        }.body()

    suspend fun getListingPhoto(publicId: String): ByteArray? {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/passenger/vehicle-listings/$publicId/photo").body()
        } catch (exception: ClientRequestException) {
            if (exception.response.status == HttpStatusCode.NotFound) {
                null
            } else {
                throw exception
            }
        }
    }
}
