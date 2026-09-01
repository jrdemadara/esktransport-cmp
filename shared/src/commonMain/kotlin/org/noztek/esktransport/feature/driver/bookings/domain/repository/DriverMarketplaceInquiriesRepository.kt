package org.noztek.esktransport.feature.driver.bookings.domain.repository

import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry

interface DriverMarketplaceInquiriesRepository {
    suspend fun getInquiries(): Result<List<DriverMarketplaceInquiry>>
}
