package org.noztek.esktransport.feature.passenger.marketplace.domain.repository

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType

interface MarketplaceRepository {
    suspend fun getRentalVehicleTypes(): Result<List<MarketplaceVehicleType>>
    suspend fun getRentalListings(vehicleTypeCode: String?): Result<List<MarketplaceListing>>
    suspend fun getListing(publicId: String): Result<MarketplaceListing>
}
