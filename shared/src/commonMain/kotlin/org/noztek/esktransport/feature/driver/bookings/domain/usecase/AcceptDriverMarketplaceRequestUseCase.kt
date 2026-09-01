package org.noztek.esktransport.feature.driver.bookings.domain.usecase

import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class AcceptDriverMarketplaceRequestUseCase(
    private val repository: DriverMarketplaceInquiriesRepository,
) {
    suspend operator fun invoke(requestPublicId: String) = repository.acceptRequest(requestPublicId)
}
