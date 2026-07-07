package org.noztek.esktransport.feature.rider.trip_navigation.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RiderTripFeedbackRequestDto(
    val rating: Int,
    val comment: String? = null,
)
