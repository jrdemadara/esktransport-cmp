package org.noztek.esktransport.feature.rider.trip_navigation.domain.usecase

import org.noztek.esktransport.feature.rider.trip_navigation.domain.repository.RiderTripNavigationRepository

class UpdateRiderTripLocationUseCase(
    private val repository: RiderTripNavigationRepository,
) {
    suspend operator fun invoke(
        bookingPublicId: String,
        latitude: Double,
        longitude: Double,
        bearing: Double?,
        speedKph: Double?,
        accuracyM: Double?,
        recordedAt: String?,
        phase: String?,
    ): Result<Unit> {
        return repository.updateTripLocation(
            bookingPublicId = bookingPublicId,
            latitude = latitude,
            longitude = longitude,
            bearing = bearing,
            speedKph = speedKph,
            accuracyM = accuracyM,
            recordedAt = recordedAt,
            phase = phase,
        )
    }
}

