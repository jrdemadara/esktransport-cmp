package org.noztek.esktransport.core.realtime.model

data class DriverOnboardingStatusChangedEvent(
    val driverId: Long,
    val driverUserId: Long?,
    val step: String?,
    val status: String?,
    val message: String?,
    val reviewedAt: String?,
)

fun DriverOnboardingStatusChangedEvent.matchesDriver(currentDriverId: Long?): Boolean {
    return currentDriverId == null || currentDriverId == driverId
}

fun DriverOnboardingStatusChangedEvent.displayMessage(): String {
    message?.takeIf { it.isNotBlank() }?.let { return it }

    return when (step) {
        "identity_verification" -> when (status) {
            "approved" -> "Your identity verification has been approved."
            "rejected" -> "Your identity verification needs an update."
            else -> "Your identity verification status has been updated."
        }
        "vehicle_registration" -> when (status) {
            "approved" -> "Your vehicle registration has been approved."
            "rejected" -> "Your vehicle registration needs an update."
            else -> "Your vehicle registration status has been updated."
        }
        "service_radius" -> "Your service zone setup has been updated."
        else -> "Your driver setup status has been updated."
    }
}
