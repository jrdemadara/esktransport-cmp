package org.noztek.esktransport.feature.passenger.activity.presentation

import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBooking
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerPendingBooking
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerRideActivity

data class ActivityUiState(
    val isLoadingActiveBooking: Boolean = true,
    val isLoadingActivity: Boolean = true,
    val activeBooking: ActiveBooking? = null,
    val recentRides: List<PassengerRideActivity> = emptyList(),
    val pendingBookings: List<PassengerPendingBooking> = emptyList(),
    val errorMessage: String? = null,
)
