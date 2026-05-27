package org.noztek.esktransport.feature.driver.home.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverAvailabilityRequestDto(
    @SerialName("is_available")
    val isAvailable: Boolean,
)

@Serializable
data class DriverAvailabilityResponseDto(
    val message: String,
    val data: DriverAvailabilityDataDto,
)

@Serializable
data class DriverAvailabilityDataDto(
    val status: String,
    @SerialName("is_available")
    val isAvailable: Boolean,
)
