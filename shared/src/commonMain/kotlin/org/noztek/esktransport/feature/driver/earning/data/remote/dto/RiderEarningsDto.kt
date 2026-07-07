package org.noztek.esktransport.feature.driver.earning.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RiderEarningsResponseDto(
    val data: RiderEarningsDashboardDto,
)

@Serializable
data class RiderEarningsDashboardDto(
    val currency: String = "PHP",
    val today: RiderEarningsTodayDto,
    @SerialName("recent_settlements")
    val recentSettlements: List<RiderEarningsSettlementDto> = emptyList(),
)

@Serializable
data class RiderEarningsTodayDto(
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("gross_fare")
    val grossFare: Double = 0.0,
    @SerialName("platform_fee")
    val platformFee: Double = 0.0,
    @SerialName("net_earning")
    val netEarning: Double = 0.0,
    val from: String? = null,
    val to: String? = null,
)

@Serializable
data class RiderEarningsSettlementDto(
    @SerialName("public_id")
    val publicId: String,
    @SerialName("gross_fare")
    val grossFare: Double = 0.0,
    @SerialName("platform_fee")
    val platformFee: Double = 0.0,
    @SerialName("net_earning")
    val netEarning: Double = 0.0,
    val currency: String = "PHP",
    @SerialName("settled_at")
    val settledAt: String? = null,
)
