package org.noztek.esktransport.feature.driver.bookings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.bookings.data.remote.DriverMarketplaceInquiriesApi
import org.noztek.esktransport.feature.driver.bookings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry
import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class DriverMarketplaceInquiriesRepositoryImpl(
    private val api: DriverMarketplaceInquiriesApi,
) : DriverMarketplaceInquiriesRepository {
    override suspend fun getInquiries(): Result<List<DriverMarketplaceInquiry>> {
        return try {
            Result.success(api.getInquiries().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load bookings.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun acceptRequest(requestPublicId: String, quotedAmount: Double, currency: String, ownerNote: String?): Result<Unit> {
        return try {
            api.acceptRequest(requestPublicId, quotedAmount, currency, ownerNote)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to accept request.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun startRequest(requestPublicId: String): Result<Unit> = runAction("Failed to start service.") { api.startRequest(requestPublicId) }
    override suspend fun completeRequest(requestPublicId: String): Result<Unit> = runAction("Failed to complete service.") { api.completeRequest(requestPublicId) }

    private suspend fun runAction(message: String, action: suspend () -> Unit): Result<Unit> {
        return try { action(); Result.success(Unit) } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, message)))
        }
    }
}
