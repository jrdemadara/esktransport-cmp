package org.noztek.esktransport.feature.driver.home.domain.usecase

import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeApi

class ExpireDriverHomeOfferUseCase(
    private val api: DriverHomeApi,
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
