package org.noztek.esktransport.feature.common.active_booking.domain.usecase

import org.noztek.esktransport.feature.common.active_booking.domain.repository.ActiveBookingRepository

class GetDriverActiveBookingUseCase(
    private val repository: ActiveBookingRepository,
) {
    suspend operator fun invoke() = repository.getDriverActiveBooking()
}
