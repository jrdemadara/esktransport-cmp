package org.noztek.esktransport.feature.passenger.marketplace.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.MarketplaceApi
import org.noztek.esktransport.feature.passenger.marketplace.data.remote.dto.CreateMarketplaceServiceRequestDto
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.CreateMarketplaceServiceRequestPayload
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest
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

    override suspend fun getListingPhoto(publicId: String): Result<ByteArray?> {
        return try {
            Result.success(api.getListingPhoto(publicId))
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load vehicle photo.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun sendRequest(
        payload: CreateMarketplaceServiceRequestPayload,
    ): Result<MarketplaceServiceRequest> {
        return try {
            Result.success(
                api.sendRequest(
                    publicId = payload.listingPublicId,
                    request = CreateMarketplaceServiceRequestDto(
                        requestedStartAt = payload.requestedStartAt,
                        requestedEndAt = payload.requestedEndAt,
                        pickupLabel = payload.pickupLabel,
                        destinationLabel = payload.destinationLabel,
                        passengerCount = payload.passengerCount,
                        cargoDescription = payload.cargoDescription,
                        cargoWeightKg = payload.cargoWeightKg,
                        cargoPieces = payload.cargoPieces,
                        customerNote = payload.customerNote,
                    ),
                ).data.toDomain(),
            )
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to send request.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getRequest(publicId: String): Result<MarketplaceServiceRequest> = runRequest("Failed to load service request.") { api.getRequest(publicId).data.toDomain() }
    override suspend fun getRequests(): Result<List<MarketplaceServiceRequest>> = runRequest("Failed to load service requests.") { api.getRequests().data.map { it.toDomain() } }
    override suspend fun confirmRequest(publicId: String): Result<MarketplaceServiceRequest> = runRequest("Failed to confirm service request.") { api.confirmRequest(publicId).data.toDomain() }
    override suspend fun cancelRequest(publicId: String): Result<MarketplaceServiceRequest> = runRequest("Failed to cancel service request.") { api.cancelRequest(publicId).data.toDomain() }
    override suspend fun completeRequest(publicId: String, cashAmount: Double, currency: String): Result<MarketplaceServiceRequest> = runRequest("Failed to complete service request.") { api.completeRequest(publicId, cashAmount, currency).data.toDomain() }

    private suspend fun <T> runRequest(message: String, block: suspend () -> T): Result<T> {
        return try { Result.success(block()) } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, message)))
        }
    }
}
