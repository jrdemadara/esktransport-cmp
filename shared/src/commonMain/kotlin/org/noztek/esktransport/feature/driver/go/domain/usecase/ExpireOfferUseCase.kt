package org.noztek.esktransport.feature.driver.go.domain.usecase

import org.noztek.esktransport.feature.driver.go.data.remote.GoApi

class ExpireOfferUseCase(
    private val api: GoApi,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return try {
            api.expireBookingOffer(bookingPublicId)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}
