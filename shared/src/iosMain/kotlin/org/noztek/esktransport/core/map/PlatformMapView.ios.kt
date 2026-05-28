package org.noztek.esktransport.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

@Composable
actual fun PlatformMapView(
    modifier: Modifier,
    config: MapboxConfig,
    cameraCenter: MapPoint,
    cameraDefaults: MapCameraDefaults,
    markers: List<MapMarker>,
    routeLines: List<MapRouteLine>,
) {
    if (config.hasAccessToken) {
        val adaptiveStyle = if (isSystemInDarkTheme()) MapboxStyle.DARK else MapboxStyle.LIGHT
        val request = remember(config, adaptiveStyle, cameraCenter, cameraDefaults) {
            IosMapboxViewRequest(
                accessToken = config.accessToken,
                styleUri = adaptiveStyle.uri,
                latitude = cameraCenter.latitude,
                longitude = cameraCenter.longitude,
                zoom = cameraDefaults.zoom,
                pitch = cameraDefaults.pitch,
                bearing = cameraDefaults.bearing,
            )
        }

        val fallbackView = remember { UIView() }
        UIKitView(
            modifier = modifier.fillMaxSize(),
            factory = {
                IosMapboxBridge.createMapView(request) ?: fallbackView
            },
            update = { view ->
                IosMapboxBridge.updateMapView(view, request)
            },
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Mapbox token is not configured.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
