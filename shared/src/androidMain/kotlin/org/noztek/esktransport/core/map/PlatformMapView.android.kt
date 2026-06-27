package org.noztek.esktransport.core.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.bindgen.Value
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
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
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.delay
import androidx.core.graphics.createBitmap
import kotlin.time.Duration.Companion.milliseconds

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
    val isDarkMode = isSystemInDarkTheme()
    val adaptiveStyle = if (isDarkMode) MapboxStyle.DARK else MapboxStyle.LIGHT
    val currentOnCameraMoving = rememberUpdatedState(onCameraMoving)
    val currentOnCameraIdle = rememberUpdatedState(onCameraIdle)
    val userLocationColor = Color(0xFF2563EB).toArgb()

    MapboxMap(
        modifier = modifier,
        mapViewportState = viewportState,
        style = { MapStyle(style = adaptiveStyle.uri) },
    ) {
        MapEffect(showUserLocation, userLocationColor, cameraDefaults) { mapView ->
            val location = mapView.location
            if (!showUserLocation) {
                location.enabled = false
                return@MapEffect
            }

            var centeredOnFirstLocation = false
            val positionListener = OnIndicatorPositionChangedListener { point ->
                if (centeredOnFirstLocation) return@OnIndicatorPositionChangedListener
                centeredOnFirstLocation = true
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(cameraDefaults.zoom)
                        .pitch(cameraDefaults.pitch)
                        .bearing(cameraDefaults.bearing)
                        .build(),
                )
            }

            location.locationPuck = createDriverLocationPuck(userLocationColor)
            location.pulsingColor = userLocationColor.withAlpha(0.36f)
            location.pulsingMaxRadius = 42f
            location.pulsingEnabled = true
            location.puckBearingEnabled = false
            location.enabled = true
            location.addOnIndicatorPositionChangedListener(positionListener)

            try {
                kotlinx.coroutines.awaitCancellation()
            } finally {
                location.removeOnIndicatorPositionChangedListener(positionListener)
                location.pulsingEnabled = false
                location.enabled = false
            }
        }

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
                    delay(50L.milliseconds)
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

private fun createDriverLocationPuck(color: Int): LocationPuck2D {
    return LocationPuck2D(
        topImage = ImageHolder.from(createDriverLocationPuckBitmap(color)),
    )
}

private fun createDriverLocationPuckBitmap(color: Int): Bitmap {
    val size = 72
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val center = size / 2f

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 28f, outerPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 18f, innerPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.withAlpha(0.18f)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(center, center, 30f, strokePaint)

    return bitmap
}

private fun Int.withAlpha(alpha: Float): Int {
    return AndroidColor.argb(
        (alpha.coerceIn(0f, 1f) * 255f).toInt(),
        AndroidColor.red(this),
        AndroidColor.green(this),
        AndroidColor.blue(this),
    )
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
