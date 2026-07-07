package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession

data class TripTrackingUIState(
    val isLoading: Boolean = false,
    val isCancelling: Boolean = false,
    val isSubmittingFeedback: Boolean = false,
    val showFeedback: Boolean = false,
    val tripSession: TripTrackingSession? = null,
    val stage: TripTrackingStage = TripTrackingStage.ToPickup,
    val riderToPickupRoute: List<MapPoint> = emptyList(),
    val driverToDestinationRoute: List<MapPoint> = emptyList(),
    val pickupToDestinationRoute: List<MapPoint> = emptyList(),
    val error: String? = null,
)

enum class TripTrackingStage {
    ToPickup,
    ArrivedPickup,
    ToDropoff,
    Completed,
}
