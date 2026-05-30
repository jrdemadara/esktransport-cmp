package org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RiderTripLocationUpdateRequestDto(
    val lat: Double,
    val lng: Double,
    val bearing: Double? = null,
    @SerialName("speed_kph")
    val speedKph: Double? = null,
    @SerialName("accuracy_m")
    val accuracyM: Double? = null,
    @SerialName("recorded_at")
    val recordedAt: String? = null,
    val phase: String? = null,
)
