package org.noztek.esktransport.feature.passenger.marketplace.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.MarketplaceApi
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType
import org.noztek.esktransport.feature.passenger.marketplace.domain.repository.MarketplaceRepository

class MarketplaceRepositoryImpl(
    private val api: MarketplaceApi,
) : MarketplaceRepository {
    override suspend fun getRentalVehicleTypes(): Result<List<MarketplaceVehicleType>> {
        return try {
            Result.success(api.getRentalVehicleTypes().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicle types.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getRentalListings(vehicleTypeCode: String?): Result<List<MarketplaceListing>> {
        return try {
            Result.success(api.getRentalListings(vehicleTypeCode).data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load rental listings.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getListing(publicId: String): Result<MarketplaceListing> {
        return try {
            Result.success(api.getListing(publicId).data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load listing details.")
            Result.failure(IllegalStateException(message))
        }
    }
}
