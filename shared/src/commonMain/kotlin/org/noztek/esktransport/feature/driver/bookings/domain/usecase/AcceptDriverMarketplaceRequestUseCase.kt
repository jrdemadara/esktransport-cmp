package org.noztek.esktransport.feature.driver.bookings.domain.usecase

import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class AcceptDriverMarketplaceRequestUseCase(
    private val repository: DriverMarketplaceInquiriesRepository,
) {
    suspend operator fun invoke(requestPublicId: String, quotedAmount: Double, currency: String = "PHP", ownerNote: String? = null) = repository.acceptRequest(requestPublicId, quotedAmount, currency, ownerNote)
}
