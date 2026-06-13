package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

@Composable
actual fun BookingReviewMap(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: GeoPoint,
    destinationPoint: GeoPoint,
    routePoints: List<GeoPoint>,
) {
    val route = routePoints.map { MapPoint(it.latitude, it.longitude) }.ifEmpty {
        listOf(
            MapPoint(pickupPoint.latitude, pickupPoint.longitude),
            MapPoint(destinationPoint.latitude, destinationPoint.longitude),
        )
    }
    PlatformMapView(
        modifier = modifier,
        config = mapboxConfig,
        cameraCenter = MapPoint(pickupPoint.latitude, pickupPoint.longitude),
        cameraDefaults = MapCameraDefaults(zoom = 14.0, pitch = 45.0),
        markers = listOf(
            MapMarker("pickup", MapPoint(pickupPoint.latitude, pickupPoint.longitude), Color(0xFF2563EB), 8.0),
            MapMarker("destination", MapPoint(destinationPoint.latitude, destinationPoint.longitude), Color(0xFFEF4444), 8.0),
        ),
        routeLines = listOf(
            MapRouteLine("pickup-destination", route, Color(0xFF2563EB), 6.0, animatedAntPath = true),
        ),
    )
}
