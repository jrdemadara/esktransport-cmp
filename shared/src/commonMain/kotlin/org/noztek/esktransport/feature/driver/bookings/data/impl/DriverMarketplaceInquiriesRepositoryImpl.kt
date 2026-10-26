package org.noztek.esktransport.feature.driver.bookings.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.driver.bookings.data.remote.DriverMarketplaceInquiriesApi
import org.noztek.esktransport.feature.driver.bookings.data.remote.dto.toDomain
import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class DriverMarketplaceInquiriesRepositoryImpl(
    private val api: DriverMarketplaceInquiriesApi,
) : DriverMarketplaceInquiriesRepository {
    override suspend fun getInbox(): Result<org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInbox> {
        return try {
            Result.success(api.getInbox().toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load bookings.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun getRequest(requestPublicId: String): Result<org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceRequest> = runResult("Failed to load request details.") { api.getRequest(requestPublicId).data.toDomain() }
    override suspend fun markThreadRead(threadId: Long): Result<Unit> = runResult("Failed to mark inquiry as read.") { api.markThreadRead(threadId) }

    override suspend fun acceptRequest(requestPublicId: String, quotedAmount: Double, currency: String, ownerNote: String?): Result<Unit> {
        return try {
            api.acceptRequest(requestPublicId, quotedAmount, currency, ownerNote)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to accept request.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun rejectRequest(requestPublicId: String): Result<Unit> = runAction("Failed to reject request.") { api.rejectRequest(requestPublicId) }

    override suspend fun startRequest(requestPublicId: String): Result<Unit> = runAction("Failed to start service.") { api.startRequest(requestPublicId) }
    override suspend fun completeRequest(requestPublicId: String): Result<Unit> = runAction("Failed to complete service.") { api.completeRequest(requestPublicId) }

    private suspend fun runAction(message: String, action: suspend () -> Unit): Result<Unit> {
        return try { action(); Result.success(Unit) } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, message)))
        }
    }

    private suspend fun <T> runResult(message: String, action: suspend () -> T): Result<T> {
        return try { Result.success(action()) } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(ApiErrorParser.parse(throwable, message)))
        }
    }
}
