package org.noztek.esktransport.feature.passenger.booking_review.presentation

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

@Composable
actual fun BookingReviewMap(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: GeoPoint,
    destinationPoint: GeoPoint,
    routePoints: List<GeoPoint>,
) {
    val context = LocalContext.current
    val styleUri = if (isSystemInDarkTheme()) Style.DARK else Style.MAPBOX_STREETS
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
    val antAnimator = remember { ValueAnimator() }
    DisposableEffect(Unit) { onDispose { antAnimator.cancel() } }

    LaunchedEffect(styleUri, pickupPoint, destinationPoint, routePoints) {
        val pickup = Point.fromLngLat(pickupPoint.longitude, pickupPoint.latitude)
        val destination = Point.fromLngLat(destinationPoint.longitude, destinationPoint.latitude)
        val path = if (routePoints.isNotEmpty()) routePoints.map { Point.fromLngLat(it.longitude, it.latitude) } else listOf(pickup, destination)
        mapView.getMapboxMap().loadStyleUri(styleUri) { style ->
            val sourceId = "booking-ant-source"
            val bgLayerId = "booking-ant-bg"
            val dashLayerId = "booking-ant-dash"
            style.addSource(
                geoJsonSource(sourceId) {
                    featureCollection(FeatureCollection.fromFeatures(listOf(Feature.fromGeometry(LineString.fromLngLats(path)))))
                },
            )
            style.addLayer(
                LineLayer(bgLayerId, sourceId)
                    .lineColor("#2563EB")
                    .lineWidth(6.0)
                    .lineOpacity(0.0)
                    .lineCap(LineCap.BUTT)
                    .lineJoin(LineJoin.BEVEL),
            )
            style.addLayer(
                LineLayer(dashLayerId, sourceId)
                    .lineColor("#2563EB")
                    .lineWidth(6.0)
                    .lineDasharray(listOf(0.0, 4.0, 3.0))
                    .lineCap(LineCap.BUTT)
                    .lineJoin(LineJoin.BEVEL),
            )

            val sequence = arrayOf(
                doubleArrayOf(0.0, 4.0, 3.0),
                doubleArrayOf(0.5, 4.0, 2.5),
                doubleArrayOf(1.0, 4.0, 2.0),
                doubleArrayOf(1.5, 4.0, 1.5),
                doubleArrayOf(2.0, 4.0, 1.0),
                doubleArrayOf(2.5, 4.0, 0.5),
                doubleArrayOf(3.0, 4.0, 0.0),
                doubleArrayOf(0.0, 0.5, 3.0, 3.5),
                doubleArrayOf(0.0, 1.0, 3.0, 3.0),
                doubleArrayOf(0.0, 1.5, 3.0, 2.5),
                doubleArrayOf(0.0, 2.0, 3.0, 2.0),
                doubleArrayOf(0.0, 2.5, 3.0, 1.5),
                doubleArrayOf(0.0, 3.0, 3.0, 1.0),
                doubleArrayOf(0.0, 3.5, 3.0, 0.5),
            )
            antAnimator.cancel()
            antAnimator.setIntValues(0, Int.MAX_VALUE)
            antAnimator.duration = 10_000_000L
            antAnimator.interpolator = LinearInterpolator()
            antAnimator.repeatCount = ValueAnimator.INFINITE
            antAnimator.addUpdateListener { animator ->
                val step = ((animator.currentPlayTime / 50L) % sequence.size).toInt()
                val values = sequence[step].map { Value.valueOf(it) }.toMutableList()
                style.setStyleLayerProperty(dashLayerId, "line-dasharray", Value.valueOf(values))
            }
            antAnimator.start()

            val fittedCamera = mapView.getMapboxMap()
                .cameraForCoordinates(path, EdgeInsets(100.0, 80.0, 410.0, 80.0), null, 45.0)
            val camera = fittedCamera
                .toBuilder()
                .zoom((fittedCamera.zoom ?: 14.0) + 0.35)
                .build()
            mapView.getMapboxMap().setCamera(camera)

            val circleManager = mapView.annotations.createCircleAnnotationManager()
            circleManager.deleteAll()
            circleManager.create(CircleAnnotationOptions().withPoint(pickup).withCircleRadius(18.0).withCircleColor("#FFFFFF").withCircleOpacity(0.48))
            circleManager.create(CircleAnnotationOptions().withPoint(pickup).withCircleRadius(12.0).withCircleColor("#FFFFFF").withCircleOpacity(0.86))
            circleManager.create(CircleAnnotationOptions().withPoint(pickup).withCircleRadius(7.0).withCircleColor("#2563EB"))

            val pointManager = mapView.annotations.createPointAnnotationManager()
            pointManager.deleteAll()
            runCatching {
                context.assets.open("$ComposeResourceAssetRoot/drawable/map_pin_red.png").use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }.getOrNull()?.let { markerBitmap ->
                pointManager.create(
                    PointAnnotationOptions()
                        .withPoint(destination)
                        .withIconImage(markerBitmap)
                        .withIconAnchor(IconAnchor.BOTTOM)
                        .withIconSize(0.22),
                )
            } ?: circleManager.create(CircleAnnotationOptions().withPoint(destination).withCircleRadius(8.0).withCircleColor("#EF4444").withCircleStrokeColor("#FFFFFF").withCircleStrokeWidth(2.0))
        }
    }

    AndroidView(modifier = modifier, factory = { mapView })
}

private const val ComposeResourceAssetRoot = "composeResources/asktransport_cmp.shared.generated.resources"
