package org.noztek.esktransport.feature.driver.home.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverHomeStatsResponseDto(
    val data: DriverHomeStatsDto,
)

@Serializable
data class DriverHomeStatsDto(
    @SerialName("total_trips")
    val totalTrips: Int,
    @SerialName("online_seconds")
    val onlineSeconds: Long,
    val rating: DriverRatingDto,
)

@Serializable
data class DriverRatingDto(
    val value: Double? = null,
    val label: String,
    val max: Int,
)
