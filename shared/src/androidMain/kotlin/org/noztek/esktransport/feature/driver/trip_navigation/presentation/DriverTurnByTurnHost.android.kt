package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView

@Composable
actual fun DriverTurnByTurnHost(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: MapPoint,
    destinationPoint: MapPoint,
    routePoints: List<MapPoint>,
    pickupConfirmed: Boolean,
) {
    val path = routePoints.takeIf { it.size >= 2 } ?: listOf(pickupPoint, destinationPoint)
    PlatformMapView(
        modifier = modifier,
        config = mapboxConfig,
        cameraCenter = if (pickupConfirmed) destinationPoint else pickupPoint,
        cameraDefaults = MapCameraDefaults(zoom = 13.2, pitch = 30.0),
        markers = listOf(
            MapMarker("pickup", pickupPoint, Color(0xFFF59E0B), 7.0),
            MapMarker("destination", destinationPoint, Color(0xFFEF4444), 7.0),
        ),
        routeLines = listOf(
            MapRouteLine("pickup-destination", path, Color(0xFF3B82F6), 6.0),
        ),
    )
}
