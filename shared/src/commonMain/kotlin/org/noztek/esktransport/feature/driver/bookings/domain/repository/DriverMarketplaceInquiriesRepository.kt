package org.noztek.esktransport.feature.driver.bookings.domain.repository

import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInbox
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceRequest

interface DriverMarketplaceInquiriesRepository {
    suspend fun getInbox(): Result<DriverMarketplaceInbox>
    suspend fun getRequest(requestPublicId: String): Result<DriverMarketplaceRequest>
    suspend fun markThreadRead(threadId: Long): Result<Unit>
    suspend fun acceptRequest(requestPublicId: String, quotedAmount: Double, currency: String, ownerNote: String?): Result<Unit>
    suspend fun startRequest(requestPublicId: String): Result<Unit>
    suspend fun completeRequest(requestPublicId: String): Result<Unit>
}
