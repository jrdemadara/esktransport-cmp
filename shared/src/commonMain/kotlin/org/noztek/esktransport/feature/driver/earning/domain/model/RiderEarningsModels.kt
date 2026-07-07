package org.noztek.esktransport.feature.driver.earning.domain.model

data class RiderEarningsDashboard(
    val currency: String,
    val today: RiderEarningsToday,
    val recentSettlements: List<RiderEarningsSettlement>,
)

data class RiderEarningsToday(
    val completedTrips: Int,
    val grossFare: Double,
    val platformFee: Double,
    val netEarning: Double,
)

data class RiderEarningsSettlement(
    val publicId: String,
    val grossFare: Double,
    val platformFee: Double,
    val netEarning: Double,
    val currency: String,
    val settledAt: String?,
)
