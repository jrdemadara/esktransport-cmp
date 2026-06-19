package org.noztek.esktransport.core.realtime.model

data class DriverOnboardingStatusChangedEvent(
    val driverId: Long,
    val driverUserId: Long?,
    val step: String?,
    val status: String?,
    val message: String?,
    val reviewedAt: String?,
)
