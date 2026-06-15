package org.noztek.esktransport.feature.common.active_booking.domain.repository

import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBooking

interface ActiveBookingRepository {
    suspend fun getPassengerActiveBooking(): Result<ActiveBooking?>
    suspend fun getDriverActiveBooking(): Result<ActiveBooking?>
}
