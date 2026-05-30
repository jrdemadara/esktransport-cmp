package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession

data class TripNavigationUiState(
    val isLoading: Boolean = true,
    val tripSession: RiderTripSession? = null,
    val routePoints: List<MapPoint> = emptyList(),
    val distanceMeters: Double? = null,
    val durationSeconds: Double? = null,
    val nextInstruction: String? = null,
    val message: String? = null,
)
