package org.noztek.esktransport.feature.passenger.marketplace.domain.repository

import org.noztek.esktransport.feature.passenger.marketplace.domain.model.CreateMarketplaceServiceRequestPayload
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceVehicleType

interface MarketplaceRepository {
    suspend fun getRentalVehicleTypes(): Result<List<MarketplaceVehicleType>>
    suspend fun getRentalListings(vehicleTypeCode: String?): Result<List<MarketplaceListing>>
    suspend fun getListing(publicId: String): Result<MarketplaceListing>
    suspend fun getListingPhoto(publicId: String): Result<ByteArray?>
    suspend fun sendRequest(payload: CreateMarketplaceServiceRequestPayload): Result<MarketplaceServiceRequest>
    suspend fun getRequest(publicId: String): Result<MarketplaceServiceRequest>
    suspend fun confirmRequest(publicId: String): Result<MarketplaceServiceRequest>
    suspend fun cancelRequest(publicId: String): Result<MarketplaceServiceRequest>
    suspend fun completeRequest(publicId: String, cashAmount: Double, currency: String): Result<MarketplaceServiceRequest>
}
