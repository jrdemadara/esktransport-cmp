package org.noztek.esktransport.feature.driver.bookings.domain.repository

import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry

interface DriverMarketplaceInquiriesRepository {
    suspend fun getInquiries(): Result<List<DriverMarketplaceInquiry>>
    suspend fun acceptRequest(requestPublicId: String, quotedAmount: Double, currency: String, ownerNote: String?): Result<Unit>
    suspend fun startRequest(requestPublicId: String): Result<Unit>
    suspend fun completeRequest(requestPublicId: String): Result<Unit>
}
