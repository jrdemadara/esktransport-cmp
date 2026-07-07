@file:OptIn(com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI::class)

package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.speedlimit.api.MapboxSpeedInfoApi
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRouteOverviewButton
import com.mapbox.navigation.ui.components.speedlimit.view.MapboxSpeedInfoView
import com.mapbox.navigation.ui.components.voice.view.MapboxSoundButton
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import java.util.Locale
import kotlin.math.abs

@Composable
actual fun DriverTurnByTurnHost(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: MapPoint,
    destinationPoint: MapPoint,
    routePoints: List<MapPoint>,
    pickupConfirmed: Boolean,
    onLocationChanged: (DriverNavigationLocation) -> Unit,
) {
    val context = LocalContext.current
    val currentOnLocationChanged = rememberUpdatedState(onLocationChanged)
    val host = remember(context, mapboxConfig.accessToken) {
        AndroidDriverTurnByTurnHost(
            context = context,
            accessToken = mapboxConfig.accessToken,
            onLocationChanged = { location -> currentOnLocationChanged.value(location) },
        )
    }

    LaunchedEffect(pickupPoint, destinationPoint, routePoints, pickupConfirmed) {
        host.updateTrip(
            pickupPoint = pickupPoint,
            destinationPoint = destinationPoint,
            routePoints = routePoints,
            pickupConfirmed = pickupConfirmed,
        )
    }

    DisposableEffect(host) {
        onDispose { host.dispose() }
    }

    AndroidView(
        modifier = modifier,
        factory = { host.view },
    )
}

private class AndroidDriverTurnByTurnHost(
    private val context: Context,
    private val accessToken: String,
    private val onLocationChanged: (DriverNavigationLocation) -> Unit,
) {
    val view: FrameLayout = FrameLayout(context)
    private val configuredAccessToken = accessToken.also { MapboxOptions.accessToken = it }

    private val mapView = MapView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
    private val maneuverView = MapboxManeuverView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP,
        ).apply {
            setMargins(8.dp, 64.dp, 8.dp, 0)
        }
    }
    private val speedInfoView = MapboxSpeedInfoView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.START or Gravity.TOP,
        ).apply {
            setMargins(16.dp, 198.dp, 0, 0)
        }
    }
    private val soundButton = MapboxSoundButton(context).apply {
        unmute()
        minimumWidth = 48.dp
        minimumHeight = 48.dp
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.END or Gravity.TOP,
        ).apply {
            setMargins(0, 198.dp, 16.dp, 0)
        }
    }
    private val routeOverviewButton = MapboxRouteOverviewButton(context).apply {
        minimumWidth = 48.dp
        minimumHeight = 48.dp
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.END or Gravity.TOP,
        ).apply {
            setMargins(0, 252.dp, 16.dp, 0)
        }
    }
    private val recenterButton = MapboxRecenterButton(context).apply {
        minimumWidth = 48.dp
        minimumHeight = 48.dp
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.END or Gravity.TOP,
        ).apply {
            setMargins(0, 306.dp, 16.dp, 0)
        }
    }

    private val distanceFormatterOptions = DistanceFormatterOptions.Builder(context).build()
    private val maneuverApi = MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatterOptions))
    private val speedInfoApi = MapboxSpeedInfoApi()
    private val speechLocale = Locale.US.toLanguageTag()
    private val speechApi = MapboxSpeechApi(context, speechLocale)
    private val voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(context, speechLocale)
    private val routeLineApi = MapboxRouteLineApi(
        MapboxRouteLineApiOptions.Builder()
            .vanishingRouteLineEnabled(true)
            .build(),
    )
    private val routeLineView = MapboxRouteLineView(
        MapboxRouteLineViewOptions.Builder(context)
            .build(),
    )
    private val navigationLocationProvider = NavigationLocationProvider()
    private val viewportDataSource = MapboxNavigationViewportDataSource(mapView.getMapboxMap()).apply {
        followingPadding = EdgeInsets(306.0, 64.0, 170.0, 64.0)
        overviewPadding = EdgeInsets(306.0, 64.0, 170.0, 64.0)
    }
    private val navigationCamera = NavigationCamera(
        mapView.getMapboxMap(),
        mapView.camera,
        viewportDataSource,
    )
    private val mapboxNavigation: MapboxNavigation
    private var currentStageKey: String? = null
    private var routeRequestId: Long? = null
    private var pendingPickupPoint: MapPoint? = null
    private var pendingDestinationPoint: MapPoint? = null
    private var pendingFallbackRoutePoints: List<MapPoint> = emptyList()
    private var isPickupStageConfirmed: Boolean = false
    private var waitingForLivePickupOrigin: Boolean = false
    private var isStyleLoaded: Boolean = false
    private var isVoiceMuted: Boolean = false
    private var latestRoutes: List<NavigationRoute> = emptyList()

    private val routesObserver = RoutesObserver { result ->
        latestRoutes = result.navigationRoutes
        if (latestRoutes.isEmpty()) {
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
            routeLineApi.clearRouteLine { clearValue ->
                mapView.getMapboxMap().getStyle()?.let { style ->
                    routeLineView.renderClearRouteLineValue(style, clearValue)
                }
            }
            return@RoutesObserver
        }

        speechApi.cancel()
        voiceInstructionsPlayer.clear()
        viewportDataSource.onRouteChanged(latestRoutes.first())
        viewportDataSource.evaluate()
        renderLatestRoutes()
        clearFallbackRoute()
        navigationCamera.requestNavigationCameraToFollowing()
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) = Unit

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            onLocationChanged(enhancedLocation.toDriverNavigationLocation())
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
                {},
                {},
            )
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()
            val speedInfo = speedInfoApi.updatePostedAndCurrentSpeed(
                locationMatcherResult,
                distanceFormatterOptions,
            )
            if (speedInfo != null) {
                speedInfoView.visibility = View.VISIBLE
                speedInfoView.render(speedInfo)
            }
            val pickup = pendingPickupPoint
            if (pickup != null && !isPickupStageConfirmed && waitingForLivePickupOrigin) {
                waitingForLivePickupOrigin = false
                requestRoute(
                    origin = Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude),
                    target = pickup.toPoint(),
                )
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress: RouteProgress ->
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error -> println("DriverTurnByTurnHost maneuver error=${error.errorMessage}") },
            {
                maneuverView.visibility = View.VISIBLE
                maneuverView.renderManeuvers(maneuvers)
                bringFloatingControlsToFront()
            },
        )
        routeLineApi.updateWithRouteProgress(routeProgress) { update ->
            mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderRouteLineUpdate(style, update)
            }
        }
    }

    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        if (!isVoiceMuted) {
            speechApi.generate(voiceInstructions, speechCallback)
        }
    }

    private val speechCallback = MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
        expected.fold(
            { error ->
                voiceInstructionsPlayer.play(error.fallback, voiceInstructionsPlayerCallback)
            },
            { value ->
                voiceInstructionsPlayer.play(value.announcement, voiceInstructionsPlayerCallback)
            },
        )
    }

    private val voiceInstructionsPlayerCallback = MapboxNavigationConsumer<SpeechAnnouncement> { value ->
        speechApi.clean(value)
    }

    init {
        view.addView(mapView)
        view.addView(maneuverView)
        view.addView(speedInfoView)
        view.addView(soundButton)
        view.addView(routeOverviewButton)
        view.addView(recenterButton)
        maneuverView.bringToFront()
        bringFloatingControlsToFront()
        mapView.location.setLocationProvider(navigationLocationProvider)
        mapView.location.locationPuck = createNavigationPuck()
        mapView.location.puckBearingEnabled = true
        mapView.location.enabled = true
        seedDeviceLocationPuck()

        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(context.applicationContext)
                    .build(),
            )
        }

        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        navigationCamera.registerNavigationCameraStateChangeObserver { state ->
            println("DriverTurnByTurnHost navigation camera state=$state")
            recenterButton.visibility = when (state) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> View.VISIBLE
            }
            println("DriverTurnByTurnHost recenter visible=${recenterButton.visibility == View.VISIBLE}")
            bringFloatingControlsToFront()
        }

        mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) { style ->
            isStyleLoaded = true
            routeLineView.initializeLayers(style)
            mapView.location.setLocationProvider(navigationLocationProvider)
            mapView.location.locationPuck = createNavigationPuck()
            mapView.location.puckBearingEnabled = true
            mapView.location.enabled = true
            seedDeviceLocationPuck()
            renderFallbackRoute(pendingFallbackRoutePoints, fitCamera = true)
            renderLatestRoutes()
            soundButton.visibility = View.VISIBLE
            routeOverviewButton.visibility = if (latestRoutes.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            recenterButton.visibility = View.INVISIBLE
            println(
                "DriverTurnByTurnHost style loaded soundVisible=${soundButton.visibility == View.VISIBLE} " +
                    "routeOverviewVisible=${routeOverviewButton.visibility == View.VISIBLE} " +
                    "recenterVisible=${recenterButton.visibility == View.VISIBLE}",
            )
            bringFloatingControlsToFront()
            navigationCamera.requestNavigationCameraToFollowing()
        }

        startTripSessionSafely()
    }

    fun updateTrip(
        pickupPoint: MapPoint,
        destinationPoint: MapPoint,
        routePoints: List<MapPoint>,
        pickupConfirmed: Boolean,
    ) {
        pendingPickupPoint = pickupPoint
        pendingDestinationPoint = destinationPoint
        pendingFallbackRoutePoints = routePoints.takeIf { it.size >= 2 }.orEmpty()
        isPickupStageConfirmed = pickupConfirmed

        val stageKey = buildString {
            append(if (pickupConfirmed) "dropoff" else "pickup")
            append(":")
            append(pickupPoint.latitude)
            append(",")
            append(pickupPoint.longitude)
            append(":")
            append(destinationPoint.latitude)
            append(",")
            append(destinationPoint.longitude)
            append(":")
            append(routePoints.joinToString("|") { "${it.latitude},${it.longitude}" })
        }
        if (stageKey == currentStageKey) return
        currentStageKey = stageKey

        val origin = if (pickupConfirmed) {
            pickupPoint
        } else {
            navigationLocationProvider.lastLocation?.let { location ->
                MapPoint(latitude = location.latitude, longitude = location.longitude)
            } ?: seedDeviceLocationPuck()
        }
        val target = if (pickupConfirmed) destinationPoint else pickupPoint
        waitingForLivePickupOrigin = !pickupConfirmed && origin == null
        renderFallbackRoute(
            routePoints = when {
                routePoints.size >= 2 -> routePoints
                origin != null && !origin.isSamePointAs(target) -> listOf(origin, target)
                !pickupPoint.isSamePointAs(destinationPoint) -> listOf(pickupPoint, destinationPoint)
                else -> emptyList()
            },
        )
        if (waitingForLivePickupOrigin) {
            requestFreshDeviceLocationForPickup()
            return
        }
        if (origin == null) return
        if (origin.isSamePointAs(target)) return
        requestRoute(origin = origin.toPoint(), target = target.toPoint())
    }

    fun dispose() {
        routeRequestId?.let(mapboxNavigation::cancelRouteRequest)
        routeRequestId = null
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.stopTripSession()
        maneuverApi.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        routeLineApi.cancel()
        routeLineView.cancel()
        mapView.onDestroy()
    }

    private fun requestRoute(origin: Point, target: Point) {
        routeRequestId?.let(mapboxNavigation::cancelRouteRequest)

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .coordinatesList(listOf(origin, target))
            .alternatives(false)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .steps(true)
            .bannerInstructions(true)
            .voiceInstructions(true)
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .build()

        routeRequestId = mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    if (routes.isEmpty()) return
                    routeRequestId = null
                    println("DriverTurnByTurnHost route ready routes=${routes.size} origin=$routerOrigin")
                    mapboxNavigation.setNavigationRoutes(routes)
                    soundButton.visibility = View.VISIBLE
                    soundButton.unmute()
                    routeOverviewButton.visibility = View.VISIBLE
                    routeOverviewButton.showTextAndExtend(1200L)
                    speedInfoView.visibility = View.VISIBLE
                    println(
                        "DriverTurnByTurnHost route controls soundVisible=${soundButton.visibility == View.VISIBLE} " +
                            "routeOverviewVisible=${routeOverviewButton.visibility == View.VISIBLE} " +
                            "speedVisible=${speedInfoView.visibility == View.VISIBLE} " +
                            "recenterVisible=${recenterButton.visibility == View.VISIBLE}",
                    )
                    bringFloatingControlsToFront()
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    routeRequestId = null
                    println("DriverTurnByTurnHost route failed reasons=$reasons")
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    routeRequestId = null
                    println("DriverTurnByTurnHost route canceled origin=$routerOrigin")
                }
            },
        )
    }

    private fun startTripSessionSafely() {
        runCatching {
            mapboxNavigation.startTripSessionWithPermissionCheck()
        }
    }

    private fun renderLatestRoutes() {
        if (!isStyleLoaded || latestRoutes.isEmpty()) return
        routeLineApi.setNavigationRoutes(latestRoutes) { drawData ->
            mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderRouteDrawData(style, drawData)
            }
        }
    }

    private fun bringFloatingControlsToFront() {
        speedInfoView.bringToFront()
        soundButton.bringToFront()
        routeOverviewButton.bringToFront()
        recenterButton.bringToFront()
    }

//    private fun applyDefaultMapboxButtonIcons() {
//        setButtonIcon(
//            view = soundButton,
//            drawableRes = if (isVoiceMuted) {
//                com.mapbox.navigation.ui.components.R.drawable.mapbox_ic_sound_off
//            } else {
//                com.mapbox.navigation.ui.components.R.drawable.mapbox_ic_sound_on
//            },
//        )
//        setButtonIcon(
//            view = routeOverviewButton,
//            drawableRes = com.mapbox.navigation.ui.components.R.drawable.mapbox_ic_route_overview,
//        )
//        setButtonIcon(
//            view = recenterButton,
//            drawableRes = com.mapbox.navigation.ui.components.R.drawable.mapbox_ic_recenter,
//        )
//    }

//    private fun setButtonIcon(view: View, drawableRes: Int) {
//        view.findViewById<AppCompatImageView>(com.mapbox.navigation.ui.components.R.id.iconImage)
//            ?.apply {
//                setImageResource(drawableRes)
//                setColorFilter(Color.BLACK)
//                visibility = View.VISIBLE
//            }
//    }

    private fun seedDeviceLocationPuck(): MapPoint? {
        val location = getBestLastKnownLocation() ?: return null
        applyDeviceLocation(location)
        return MapPoint(latitude = location.latitude, longitude = location.longitude)
    }

    private fun applyDeviceLocation(location: android.location.Location) {
        println("DriverTurnByTurnHost device location lat=${location.latitude} lng=${location.longitude}")
        val mapboxLocation = location.toMapboxLocation()
        onLocationChanged(mapboxLocation.toDriverNavigationLocation())
        navigationLocationProvider.changePosition(mapboxLocation, emptyList(), {}, {})
        viewportDataSource.onLocationChanged(mapboxLocation)
        viewportDataSource.evaluate()
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(): android.location.Location? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return runCatching {
            manager.getProviders(true)
                .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
                .maxByOrNull { location -> location.time }
        }.getOrNull()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshDeviceLocationForPickup() {
        if (!hasLocationPermission()) return
        val pickup = pendingPickupPoint ?: return
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.getCurrentLocation(
                provider,
                CancellationSignal(),
                context.mainExecutor,
            ) { location ->
                if (location == null || isPickupStageConfirmed) return@getCurrentLocation
                applyDeviceLocation(location)
                renderFallbackRoute(listOf(MapPoint(location.latitude, location.longitude), pickup))
                waitingForLivePickupOrigin = false
                requestRoute(
                    origin = Point.fromLngLat(location.longitude, location.latitude),
                    target = pickup.toPoint(),
                )
            }
        } else {
            manager.requestSingleUpdate(
                provider,
                { location ->
                    if (isPickupStageConfirmed) return@requestSingleUpdate
                    applyDeviceLocation(location)
                    renderFallbackRoute(listOf(MapPoint(location.latitude, location.longitude), pickup))
                    waitingForLivePickupOrigin = false
                    requestRoute(
                        origin = Point.fromLngLat(location.longitude, location.latitude),
                        target = pickup.toPoint(),
                    )
                },
                Looper.getMainLooper(),
            )
        }
    }

    private fun renderFallbackRoute(
        routePoints: List<MapPoint>,
        fitCamera: Boolean = true,
    ) {
        pendingFallbackRoutePoints = routePoints
            .filter(MapPoint::isValidCoordinate)
            .takeIf { it.size >= 2 }
            .orEmpty()
        if (!isStyleLoaded) return

        val points = pendingFallbackRoutePoints
        if (points.size < 2) {
            clearFallbackRoute()
            return
        }

        val style = mapView.getMapboxMap().getStyle() ?: return
        val linePoints = points.map(MapPoint::toPoint)
        val featureCollection = FeatureCollection.fromFeatures(
            listOf(Feature.fromGeometry(LineString.fromLngLats(linePoints))),
        )

        if (style.styleSourceExists(FALLBACK_ROUTE_SOURCE_ID)) {
            style.getSourceAs<GeoJsonSource>(FALLBACK_ROUTE_SOURCE_ID)
                ?.featureCollection(featureCollection)
        } else {
            style.addSource(
                geoJsonSource(FALLBACK_ROUTE_SOURCE_ID) {
                    featureCollection(featureCollection)
                },
            )
        }

        if (style.styleLayerExists(FALLBACK_ROUTE_LAYER_ID)) {
            style.removeStyleLayer(FALLBACK_ROUTE_LAYER_ID)
        }

        style.addLayer(
            LineLayer(FALLBACK_ROUTE_LAYER_ID, FALLBACK_ROUTE_SOURCE_ID)
                .lineColor("#2563EB")
                .lineWidth(6.0)
                .lineOpacity(0.9)
                .lineCap(LineCap.ROUND)
                .lineJoin(LineJoin.ROUND),
        )

        if (fitCamera) {
            val camera = mapView.getMapboxMap().cameraForCoordinates(
                linePoints,
                EdgeInsets(180.0, 80.0, 260.0, 80.0),
                null,
                45.0,
            )
            mapView.getMapboxMap().setCamera(camera)
        }
    }

    private fun clearFallbackRoute() {
        val style = mapView.getMapboxMap().getStyle() ?: return
        if (style.styleLayerExists(FALLBACK_ROUTE_LAYER_ID)) {
            style.removeStyleLayer(FALLBACK_ROUTE_LAYER_ID)
        }
        if (style.styleSourceExists(FALLBACK_ROUTE_SOURCE_ID)) {
            style.removeStyleSource(FALLBACK_ROUTE_SOURCE_ID)
        }
    }
}

private const val FALLBACK_ROUTE_SOURCE_ID = "driver-trip-fallback-route-source"
private const val FALLBACK_ROUTE_LAYER_ID = "driver-trip-fallback-route-layer"

private fun MapPoint.isValidCoordinate(): Boolean {
    return latitude in -90.0..90.0 && longitude in -180.0..180.0
}

private fun createNavigationPuck(): LocationPuck2D {
    return LocationPuck2D(
        bearingImage = ImageHolder.from(createPuckBitmap()),
    )
}

private fun createPuckBitmap(): Bitmap {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, 42f, circlePaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawCircle(center, center, 40f, strokePaint)

    val arrowPath = Path().apply {
        moveTo(center, 18f)
        lineTo(70f, 70f)
        lineTo(center, 58f)
        lineTo(26f, 70f)
        close()
    }
    val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(45, 128, 255)
        style = Paint.Style.FILL
    }
    canvas.drawPath(arrowPath, arrowPaint)
    return bitmap
}

private fun RouteOptions.Builder.applyDefaultNavigationOptions(): RouteOptions.Builder {
    return baseUrl(DirectionsCriteria.BASE_API_URL)
        .user(DirectionsCriteria.PROFILE_DEFAULT_USER)
        .language("en")
        .voiceUnits(DirectionsCriteria.METRIC)
}

private fun MapPoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

private fun MapPoint.isSamePointAs(other: MapPoint): Boolean {
    return abs(latitude - other.latitude) < 0.000001 &&
        abs(longitude - other.longitude) < 0.000001
}

private fun android.location.Location.toMapboxLocation(): Location {
    return Location.Builder()
        .latitude(latitude)
        .longitude(longitude)
        .timestamp(time)
        .horizontalAccuracy(accuracy.toDouble())
        .bearing(bearing.toDouble())
        .speed(speed.toDouble())
        .source(provider)
        .build()
}

private fun Location.toDriverNavigationLocation(): DriverNavigationLocation {
    return DriverNavigationLocation(
        latitude = latitude,
        longitude = longitude,
        bearing = bearing,
        speedKph = speed?.let { it * 3.6 },
        accuracyM = horizontalAccuracy,
    )
}

private val Int.dp: Int
    get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
