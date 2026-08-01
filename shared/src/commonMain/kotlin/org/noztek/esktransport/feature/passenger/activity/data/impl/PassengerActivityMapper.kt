package org.noztek.esktransport.feature.passenger.activity.data.impl

import org.noztek.esktransport.feature.passenger.activity.data.remote.dto.PassengerActivityDashboardDto
import org.noztek.esktransport.feature.passenger.activity.data.remote.dto.PassengerActivityStopDto
import org.noztek.esktransport.feature.passenger.activity.data.remote.dto.PassengerPendingBookingDto
import org.noztek.esktransport.feature.passenger.activity.data.remote.dto.PassengerRideActivityDto
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerActivityDashboard
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerActivityStop
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerPendingBooking
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerPendingBookingStatus
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerRideActivity
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerRideActivityStatus

fun PassengerActivityDashboardDto.toDomain(): PassengerActivityDashboard {
    return PassengerActivityDashboard(
        recentRides = recentRides.map { it.toDomain() },
        pendingBookings = pendingBookings.map { it.toDomain() },
    )
}

private fun PassengerRideActivityDto.toDomain(): PassengerRideActivity {
    return PassengerRideActivity(
        bookingPublicId = bookingPublicId,
        bookingType = bookingType,
        status = status.toRideStatus(),
        driverName = driverName,
        vehicleTypeCode = vehicleTypeCode,
        requestedAt = requestedAt,
        completedAt = completedAt,
        canceledAt = canceledAt,
        activityAt = activityAt,
        cancelReason = cancelReason,
        currency = currency,
        finalFare = finalFare,
        distanceKm = distanceKm,
        durationMin = durationMin,
        pickup = pickup.toDomain(),
        dropoff = dropoff.toDomain(),
    )
}

private fun PassengerPendingBookingDto.toDomain(): PassengerPendingBooking {
    return PassengerPendingBooking(
        bookingPublicId = bookingPublicId,
        bookingType = bookingType,
        status = status.toPendingStatus(),
        vehicleTypeCode = vehicleTypeCode,
        requestedAt = requestedAt,
        currency = currency,
        finalFare = finalFare,
        pickupLabel = pickupLabel,
        dropoffLabel = dropoffLabel,
    )
}

private fun PassengerActivityStopDto.toDomain(): PassengerActivityStop {
    return PassengerActivityStop(
        label = label,
        lat = lat,
        lng = lng,
    )
}

private fun String.toRideStatus(): PassengerRideActivityStatus {
    return when (lowercase()) {
        "completed" -> PassengerRideActivityStatus.Completed
        "canceled", "cancelled" -> PassengerRideActivityStatus.Cancelled
        "expired" -> PassengerRideActivityStatus.Expired
        else -> PassengerRideActivityStatus.Unknown
    }
}

private fun String.toPendingStatus(): PassengerPendingBookingStatus {
    return when (lowercase()) {
        "searching" -> PassengerPendingBookingStatus.Searching
        "offered" -> PassengerPendingBookingStatus.Offered
        else -> PassengerPendingBookingStatus.Unknown
    }
}
