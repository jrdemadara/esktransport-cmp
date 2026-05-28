package org.noztek.esktransport.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle

@Composable
actual fun PlatformMapView(
    modifier: Modifier,
    config: MapboxConfig,
    cameraCenter: MapPoint,
    cameraDefaults: MapCameraDefaults,
    markers: List<MapMarker>,
    routeLines: List<MapRouteLine>,
) {
    if (!config.hasAccessToken) {
        MissingMapToken(modifier = modifier)
        return
    }

    LaunchedEffect(config.accessToken) {
        MapboxOptions.accessToken = config.accessToken
    }

    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(cameraCenter.toPoint())
            zoom(cameraDefaults.zoom)
            pitch(cameraDefaults.pitch)
            bearing(cameraDefaults.bearing)
        }
    }

    LaunchedEffect(cameraCenter, cameraDefaults) {
        viewportState.setCameraOptions {
            center(cameraCenter.toPoint())
            zoom(cameraDefaults.zoom)
            pitch(cameraDefaults.pitch)
            bearing(cameraDefaults.bearing)
        }
    }
    val adaptiveStyle = if (isSystemInDarkTheme()) MapboxStyle.DARK else MapboxStyle.LIGHT

    MapboxMap(
        modifier = modifier,
        mapViewportState = viewportState,
        style = { MapStyle(style = adaptiveStyle.uri) },
    ) {
        routeLines.forEach { routeLine ->
            val points = remember(routeLine.points) {
                routeLine.points.map(MapPoint::toPoint)
            }
            if (points.size >= 2) {
                PolylineAnnotation(points = points) {
                    lineColor = routeLine.color
                    lineWidth = routeLine.width
                    lineOpacity = 1.0
                }
            }
        }

        markers.forEach { marker ->
            CircleAnnotation(point = marker.point.toPoint()) {
                circleColor = marker.color
                circleRadius = marker.radius
                circleStrokeColor = androidx.compose.ui.graphics.Color.White
                circleStrokeWidth = 2.0
            }
        }
    }
}

@Composable
private fun MissingMapToken(modifier: Modifier) {
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

private fun MapPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)
