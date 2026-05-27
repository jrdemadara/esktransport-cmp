package org.noztek.esktransport.feature.driver.home.domain.usecase

import org.noztek.esktransport.feature.driver.home.data.remote.DriverHomeApi

class AcceptDriverHomeOfferUseCase(
    private val api: DriverHomeApi,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return try {
            api.acceptBookingOffer(bookingPublicId)
            Result.success(Unit)
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}

