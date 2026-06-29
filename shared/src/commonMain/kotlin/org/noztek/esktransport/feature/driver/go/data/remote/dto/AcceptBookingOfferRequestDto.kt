package org.noztek.esktransport.feature.driver.go.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcceptBookingOfferRequestDto(
    val lat: Double,
    val lng: Double,
    val bearing: Double? = null,
    @SerialName("speed_kph")
    val speedKph: Double? = null,
    @SerialName("accuracy_m")
    val accuracyM: Double? = null,
    @SerialName("recorded_at")
    val recordedAt: String? = null,
)
