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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.bindgen.Value
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.delay

@Composable
actual fun PlatformMapView(
    modifier: Modifier,
    config: MapboxConfig,
    cameraCenter: MapPoint,
    cameraDefaults: MapCameraDefaults,
    markers: List<MapMarker>,
    routeLines: List<MapRouteLine>,
    showUserLocation: Boolean,
    onCameraMoving: ((MapPoint) -> Unit)?,
    onCameraIdle: ((MapPoint) -> Unit)?,
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
    val currentOnCameraMoving = rememberUpdatedState(onCameraMoving)
    val currentOnCameraIdle = rememberUpdatedState(onCameraIdle)

    MapboxMap(
        modifier = modifier,
        mapViewportState = viewportState,
        style = { MapStyle(style = adaptiveStyle.uri) },
    ) {
        MapEffect(Unit) { mapView ->
            val moveListener = object : OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    val center = mapView.mapboxMap.cameraState.center
                    currentOnCameraMoving.value?.invoke(MapPoint(center.latitude(), center.longitude()))
                }

                override fun onMove(detector: MoveGestureDetector): Boolean = false

                override fun onMoveEnd(detector: MoveGestureDetector) {
                    val center = mapView.mapboxMap.cameraState.center
                    currentOnCameraIdle.value?.invoke(MapPoint(center.latitude(), center.longitude()))
                }
            }
            mapView.gestures.addOnMoveListener(moveListener)
            try {
                kotlinx.coroutines.awaitCancellation()
            } finally {
                mapView.gestures.removeOnMoveListener(moveListener)
            }
        }
        MapEffect(routeLines) { mapView ->
            val antRoute = routeLines.firstOrNull { it.animatedAntPath && it.points.size >= 2 } ?: return@MapEffect
            val linePoints = antRoute.points.map(MapPoint::toPoint)
            val antColorHex = antRoute.color.toHexColorString()
            val sourceId = "ant-path-source"
            val bgLayerId = "ant-path-bg"
            val dashLayerId = "ant-path-dash"
            val style = mapView.mapboxMap.getStyle() ?: return@MapEffect

            if (!style.styleSourceExists(sourceId)) {
                style.addSource(
                    geoJsonSource(sourceId) {
                        featureCollection(
                            FeatureCollection.fromFeatures(
                                listOf(Feature.fromGeometry(LineString.fromLngLats(linePoints))),
                            ),
                        )
                    },
                )
            } else {
                style.getSourceAs<GeoJsonSource>(sourceId)
                    ?.featureCollection(
                        FeatureCollection.fromFeatures(
                            listOf(Feature.fromGeometry(LineString.fromLngLats(linePoints))),
                        ),
                    )
            }

            if (!style.styleLayerExists(bgLayerId)) {
                style.addLayer(
                    LineLayer(bgLayerId, sourceId)
                        .lineColor(antColorHex)
                        .lineWidth(antRoute.width)
                        .lineOpacity(0.0)
                        .lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.BUTT)
                        .lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.BEVEL),
                )
            }
            if (!style.styleLayerExists(dashLayerId)) {
                style.addLayer(
                    LineLayer(dashLayerId, sourceId)
                        .lineColor(antColorHex)
                        .lineWidth(antRoute.width)
                        .lineOpacity(1.0)
                        .lineDasharray(listOf(0.0, 4.0, 3.0))
                        .lineCap(com.mapbox.maps.extension.style.layers.properties.generated.LineCap.BUTT)
                        .lineJoin(com.mapbox.maps.extension.style.layers.properties.generated.LineJoin.BEVEL),
                )
            }

            val dashArraySequence = listOf(
                listOf(0.0, 4.0, 3.0),
                listOf(0.5, 4.0, 2.5),
                listOf(1.0, 4.0, 2.0),
                listOf(1.5, 4.0, 1.5),
                listOf(2.0, 4.0, 1.0),
                listOf(2.5, 4.0, 0.5),
                listOf(3.0, 4.0, 0.0),
                listOf(0.0, 0.5, 3.0, 3.5),
                listOf(0.0, 1.0, 3.0, 3.0),
                listOf(0.0, 1.5, 3.0, 2.5),
                listOf(0.0, 2.0, 3.0, 2.0),
                listOf(0.0, 2.5, 3.0, 1.5),
                listOf(0.0, 3.0, 3.0, 1.0),
                listOf(0.0, 3.5, 3.0, 0.5),
            )
            try {
                var step = 0
                while (true) {
                    val values = dashArraySequence[step].map { Value.valueOf(it) }.toMutableList()
                    style.setStyleLayerProperty(
                        dashLayerId,
                        "line-dasharray",
                        Value.valueOf(values),
                    )
                    step = (step + 1) % dashArraySequence.size
                    delay(50L)
                }
            } catch (_: Throwable) {
                // map recomposed/disposed; stop animation loop
            }
        }

        routeLines.forEach { routeLine ->
            if (routeLine.animatedAntPath) return@forEach
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

private fun androidx.compose.ui.graphics.Color.toHexColorString(): String {
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
