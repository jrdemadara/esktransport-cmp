package org.noztek.esktransport.feature.passenger.marketplace.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.MarketplaceListingsResponseDto
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
}
