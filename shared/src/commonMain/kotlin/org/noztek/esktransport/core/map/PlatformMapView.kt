package org.noztek.esktransport.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformMapView(
    modifier: Modifier = Modifier,
    config: MapboxConfig,
    cameraCenter: MapPoint,
    cameraDefaults: MapCameraDefaults = MapCameraDefaults(),
    markers: List<MapMarker> = emptyList(),
    routeLines: List<MapRouteLine> = emptyList(),
    showUserLocation: Boolean = false,
    onCameraMoving: ((MapPoint) -> Unit)? = null,
    onCameraIdle: ((MapPoint) -> Unit)? = null,
)
