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
import androidx.compose.ui.graphics.Color
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
    onCameraMoving: ((MapPoint) -> Unit)?,
    onCameraIdle: ((MapPoint) -> Unit)?,
) {
    if (config.hasAccessToken) {
        val adaptiveStyle = if (isSystemInDarkTheme()) MapboxStyle.DARK else MapboxStyle.LIGHT
        val antRoute = routeLines.firstOrNull { it.animatedAntPath && it.points.size >= 2 }
        val request = remember(config, adaptiveStyle, cameraCenter, cameraDefaults, onCameraMoving, onCameraIdle) {
            IosMapboxViewRequest(
                accessToken = config.accessToken,
                styleUri = adaptiveStyle.uri,
                latitude = cameraCenter.latitude,
                longitude = cameraCenter.longitude,
                zoom = cameraDefaults.zoom,
                pitch = cameraDefaults.pitch,
                bearing = cameraDefaults.bearing,
                routePoints = antRoute?.points ?: emptyList(),
                antPathEnabled = antRoute != null,
                antPathColorHex = antRoute?.color?.toHexColorString() ?: "#2563EB",
                antPathWidth = antRoute?.width ?: 6.0,
                onCameraMoving = onCameraMoving,
                onCameraIdle = onCameraIdle,
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

private fun Color.toHexColorString(): String {
    val r = (red * 255f).toInt().coerceIn(0, 255)
    val g = (green * 255f).toInt().coerceIn(0, 255)
    val b = (blue * 255f).toInt().coerceIn(0, 255)
    return buildString {
        append('#')
        append(r.toString(16).padStart(2, '0').uppercase())
        append(g.toString(16).padStart(2, '0').uppercase())
        append(b.toString(16).padStart(2, '0').uppercase())
    }
}
