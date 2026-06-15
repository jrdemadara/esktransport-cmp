package org.noztek.esktransport.feature.common.active_booking.domain.model

data class ActiveBooking(
    val bookingPublicId: String,
    val status: ActiveBookingStatus,
    val finalFare: Double?,
    val currency: String?,
    val requestedAt: String?,
    val searchExpiresAt: String?,
    val offerExpiresAt: String?,
    val pickup: ActiveBookingPoint?,
    val destination: ActiveBookingPoint?,
    val driver: ActiveBookingDriver?,
    val passenger: ActiveBookingPassenger?,
)

data class ActiveBookingPoint(
    val label: String?,
    val latitude: Double?,
    val longitude: Double?,
)

data class ActiveBookingDriver(
    val riderUserId: Long?,
    val driverPublicId: String?,
    val name: String?,
    val vehicleTypeCode: String?,
    val vehicleLabel: String?,
    val vehiclePlate: String?,
    val passengerCapacity: Int?,
)

data class ActiveBookingPassenger(
    val userId: Long?,
    val name: String?,
)

enum class ActiveBookingStatus {
    SEARCHING,
    OFFERED,
    ACCEPTED,
    ARRIVING_PICKUP,
    IN_PROGRESS,
    UNKNOWN,
}
