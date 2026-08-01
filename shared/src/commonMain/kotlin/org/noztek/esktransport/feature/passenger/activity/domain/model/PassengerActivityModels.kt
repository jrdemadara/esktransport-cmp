package org.noztek.esktransport.feature.passenger.activity.domain.model

data class PassengerActivityDashboard(
    val recentRides: List<PassengerRideActivity>,
    val pendingBookings: List<PassengerPendingBooking>,
)

data class PassengerRideActivity(
    val bookingPublicId: String,
    val bookingType: String,
    val status: PassengerRideActivityStatus,
    val driverName: String?,
    val vehicleTypeCode: String?,
    val requestedAt: String?,
    val completedAt: String?,
    val canceledAt: String?,
    val activityAt: String?,
    val cancelReason: String?,
    val currency: String,
    val finalFare: Double?,
    val distanceKm: Double?,
    val durationMin: Int?,
    val pickup: PassengerActivityStop,
    val dropoff: PassengerActivityStop,
)

enum class PassengerRideActivityStatus {
    Completed,
    Cancelled,
    Expired,
    Unknown,
}

data class PassengerPendingBooking(
    val bookingPublicId: String,
    val bookingType: String,
    val status: PassengerPendingBookingStatus,
    val vehicleTypeCode: String?,
    val requestedAt: String?,
    val currency: String,
    val finalFare: Double?,
    val pickupLabel: String?,
    val dropoffLabel: String?,
)

enum class PassengerPendingBookingStatus {
    Searching,
    Offered,
    Unknown,
}

data class PassengerActivityStop(
    val label: String?,
    val lat: Double?,
    val lng: Double?,
)
