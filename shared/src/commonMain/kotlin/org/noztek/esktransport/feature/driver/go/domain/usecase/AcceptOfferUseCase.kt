package org.noztek.esktransport.feature.driver.go.domain.usecase

import org.noztek.esktransport.core.location.CurrentLocationProvider
import org.noztek.esktransport.feature.driver.go.data.remote.GoApi

class AcceptOfferUseCase(
    private val api: GoApi,
    private val currentLocationProvider: CurrentLocationProvider,
) {
    suspend operator fun invoke(bookingPublicId: String): Result<Unit> {
        return try {
            val currentLocation = runCatching {
                currentLocationProvider.getLastKnownLocation()
            }.getOrNull()
            api.acceptBookingOffer(
                bookingPublicId = bookingPublicId,
                latitude = currentLocation?.latitude,
                longitude = currentLocation?.longitude,
            )
            Result.success(Unit)
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}
