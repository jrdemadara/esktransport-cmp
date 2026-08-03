package org.noztek.esktransport.feature.common.presence.domain.model

data class UserPresence(
    val userId: Long,
    val status: UserPresenceStatus,
    val currentRole: UserPresenceRole?,
    val currentContext: UserPresenceContext?,
    val metadata: Map<String, String>,
    val lastSeenAt: String?,
    val lastForegroundedAt: String?,
    val lastBackgroundedAt: String?,
    val lastOfflineAt: String?,
)

enum class UserPresenceStatus {
    Online,
    Backgrounded,
    Offline,
    Unknown,
}

enum class UserPresenceRole(val value: String) {
    Passenger("passenger"),
    Driver("driver"),
    Admin("admin"),
}

enum class UserPresenceContext(val value: String) {
    PassengerHome("passenger_home"),
    RidePlanner("ride_planner"),
    BookingReview("booking_review"),
    BookingSearch("booking_search"),
    LocationSearch("location_search"),
    TripTracking("trip_tracking"),
    Services("services"),
    Kudi("kudi"),
    Activity("activity"),
    Profile("profile"),
    DriverHome("driver_home"),
    DriverGo("driver_go"),
    DriverIdentityVerification("driver_identity_verification"),
    DriverVehicleRegistration("driver_vehicle_registration"),
    DriverVehicleServices("driver_vehicle_services"),
    DriverServiceZone("driver_service_zone"),
    DriverTripTracking("driver_trip_tracking"),
}
