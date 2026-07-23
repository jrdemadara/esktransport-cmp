package org.noztek.esktransport.feature.driver.trips.domain.model

data class DriverTripsDashboard(
    val currency: String,
    val summary: DriverTripsSummary,
    val trips: List<DriverTrip>,
)

data class DriverTripsSummary(
    val completedTrips: Int,
    val onlineSeconds: Long,
    val grossFare: Double,
    val platformFee: Double,
    val netEarning: Double,
    val from: String?,
    val to: String?,
)

data class DriverTrip(
    val bookingPublicId: String,
    val bookingType: String,
    val status: DriverTripStatus,
    val passengerName: String,
    val vehicleTypeCode: String?,
    val requestedAt: String?,
    val assignedAt: String?,
    val acceptedAt: String?,
    val pickupConfirmedAt: String?,
    val completedAt: String?,
    val canceledAt: String?,
    val cancelReason: String?,
    val currency: String,
    val finalFare: Double?,
    val paymentMethod: String?,
    val distanceKm: Double?,
    val durationMin: Int?,
    val pickup: DriverTripStop,
    val dropoff: DriverTripStop,
    val settlement: DriverTripSettlement?,
    val feedback: DriverTripFeedbackBundle,
)

enum class DriverTripStatus {
    Offered,
    Accepted,
    ArrivingPickup,
    InProgress,
    Completed,
    Cancelled,
    Expired,
    Unknown,
}

data class DriverTripStop(
    val label: String?,
    val lat: Double?,
    val lng: Double?,
)

data class DriverTripSettlement(
    val publicId: String,
    val grossFare: Double?,
    val platformFee: Double,
    val netEarning: Double,
    val platformFeePercentage: Double,
    val settledAt: String?,
)

data class DriverTripFeedbackBundle(
    val passengerToDriver: DriverTripFeedback?,
    val driverToPassenger: DriverTripFeedback?,
)

data class DriverTripFeedback(
    val rating: Int,
    val comment: String?,
    val submittedAt: String?,
)
