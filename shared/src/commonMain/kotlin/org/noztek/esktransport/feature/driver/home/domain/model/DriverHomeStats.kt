package org.noztek.esktransport.feature.driver.home.domain.model

data class DriverHomeStats(
    val totalTrips: Int,
    val onlineSeconds: Long,
    val rating: DriverRating,
)

data class DriverRating(
    val value: Double?,
    val label: String,
    val max: Int,
)
