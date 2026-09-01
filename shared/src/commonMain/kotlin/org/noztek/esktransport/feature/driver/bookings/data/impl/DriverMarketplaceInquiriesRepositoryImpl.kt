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
}
