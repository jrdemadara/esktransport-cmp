package org.noztek.esktransport.feature.driver.bookings.domain.usecase

import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class GetDriverMarketplaceInquiriesUseCase(
    private val repository: DriverMarketplaceInquiriesRepository,
) {
    suspend operator fun invoke() = repository.getInquiries()
}
