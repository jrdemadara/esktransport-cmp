package org.noztek.esktransport.feature.passenger.trip_tracking.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TripFeedbackRequestDto(
    val rating: Int,
    val comment: String? = null,
)
