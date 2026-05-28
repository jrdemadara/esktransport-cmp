package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

@Composable
expect fun BookingReviewMap(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: GeoPoint,
    destinationPoint: GeoPoint,
    routePoints: List<GeoPoint>,
)

