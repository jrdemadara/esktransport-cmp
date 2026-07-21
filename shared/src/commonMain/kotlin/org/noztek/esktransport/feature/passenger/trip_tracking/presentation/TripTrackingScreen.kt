package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.EllipsisVertical
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.solid.Star
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapMarkerIcon
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.core.ui.composables.common.HoldToCancelButton
import org.noztek.esktransport.core.ui.composables.common.TripFeedbackDialog
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.LatestLocation
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripPoint
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTrackingScreen(
    bookingId: String,
    onCancelled: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: TripTrackingViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.tripSession
    var initialCameraCenter by remember(bookingId) { mutableStateOf<MapPoint?>(null) }
    var initialCameraDefaults by remember(bookingId) { mutableStateOf<MapCameraDefaults?>(null) }
    var cameraIncludesDriver by remember(bookingId) { mutableStateOf(false) }
    LaunchedEffect(bookingId) { viewModel.loadTripData(bookingId) }

    DisposableEffect(viewModel, bookingId) {
        viewModel.startRealtime(bookingId)
        onDispose { viewModel.stopRealtime() }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                TripTrackingUiEvent.NavigateToBookingReview -> onCancelled()
                TripTrackingUiEvent.NavigateToHome -> onCompleted()
            }
        }
    }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = true,
        ),
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 196.dp,
        sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetContent = {
            session?.let {
                TripTrackingSheet(
                    riderName = it.riderInfo.name,
                    riderRating = it.riderInfo.rating,
                    fareLabel = it.fareLabel(),
                    vehicleLabel = "${it.riderInfo.vehicleLabel} - ${it.riderInfo.vehiclePlate}",
                    pickupLabel = it.pickupPoint.label,
                    destinationLabel = it.destinationPoint.label,
                    isCancelling = uiState.isCancelling,
                    onCancel = { viewModel.cancelTrip(bookingId) },
                )
            } ?: LoadingSheet(isLoading = uiState.isLoading)
        },
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            val center = session?.tripMarkerCenter()
            val zoom = session?.tripMarkerZoom() ?: 13.5
            val hasDriverLocation = session?.latestLocation != null
            LaunchedEffect(bookingId, center, zoom, hasDriverLocation) {
                val shouldSetInitialCamera = center != null && initialCameraCenter == null
                val shouldFitDriverOnce = center != null && hasDriverLocation && !cameraIncludesDriver
                if (shouldSetInitialCamera || shouldFitDriverOnce) {
                    initialCameraCenter = center
                    initialCameraDefaults = MapCameraDefaults(zoom = zoom + 0.6, pitch = 65.0)
                    cameraIncludesDriver = hasDriverLocation
                }
            }
            val markers = session?.let { buildTripMarkers(it, uiState.stage) }.orEmpty()
            val routeLines = buildTripRouteLines(uiState)
            val cameraCenter = initialCameraCenter
            val cameraDefaults = initialCameraDefaults
            if (cameraCenter != null && cameraDefaults != null) {
                PlatformMapView(
                    modifier = Modifier.fillMaxSize(),
                    config = mapboxConfig,
                    cameraCenter = cameraCenter,
                    cameraDefaults = cameraDefaults,
                    markers = markers,
                    routeLines = routeLines,
                )
            } else {
                TripMapPlaceholder(isLoading = uiState.isLoading)
            }

            uiState.error?.let { error ->
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(error, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            }

            TripTrackingTopAppBar(
                statusLabel = sessionStatusLabel(uiState.stage),
                modifier = Modifier.align(Alignment.TopCenter),
            )

        }
    }

    if (uiState.showFeedback) {
        TripFeedbackDialog(
            title = "Rate your ride",
            message = "Trip completed. Add a quick rating for your driver.",
            isSubmitting = uiState.isSubmittingFeedback,
            onSubmit = { rating, comment ->
                viewModel.submitFeedback(
                    bookingPublicId = bookingId,
                    rating = rating,
                    comment = comment,
                )
            },
            onDismiss = viewModel::skipFeedback,
        )
    }
}

@Composable
private fun TripMapPlaceholder(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Waiting for trip location...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingSheet(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Loading trip details...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TripTrackingSheet(
    riderName: String,
    riderRating: Double?,
    fareLabel: String?,
    vehicleLabel: String,
    pickupLabel: String,
    destinationLabel: String,
    isCancelling: Boolean,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Heroicons.Outline.User,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = riderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    riderRating?.let { rating ->
                        RiderRatingLabel(rating = rating)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = vehicleLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
            FareSummary(fareLabel = fareLabel)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

        PassengerRouteTimeline(
            modifier = Modifier.fillMaxWidth(),
            pickupLabel = pickupLabel,
            destinationLabel = destinationLabel,
            lineColor = MaterialTheme.colorScheme.outlineVariant,
        )

        HoldToCancelButton(
            isCancelling = isCancelling,
            onCancel = onCancel,
            modifier = Modifier.fillMaxWidth(),
            text = "Hold to cancel trip",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripTrackingTopAppBar(
    statusLabel: String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Trip Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "StatusDotPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "pulseScale",
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.45f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "pulseAlpha",
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .graphicsLayer(
                                    scaleX = pulseScale,
                                    scaleY = pulseScale,
                                    alpha = pulseAlpha,
                                )
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }




            }
        },
        actions = {
            IconButton(
                onClick = {},
            ) {
                Icon(
                    Heroicons.Outline.ChatBubbleOvalLeft,
                    contentDescription = "Chat",
                )
            }
            IconButton(
                onClick = {},
            ) {
                Icon(
                    Heroicons.Outline.Phone,
                    contentDescription = "Call",
                )
            }
            IconButton(
                onClick = {},
            ) {
                Icon(
                    Heroicons.Outline.EllipsisVertical,
                    contentDescription = "More options",
                )
            }
        },
    )
}

@Composable
private fun FareSummary(fareLabel: String?) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = "Fare",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = fareLabel ?: "Pending",
            style = MaterialTheme.typography.headlineMediumEmphasized,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RiderRatingLabel(rating: Double) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            Heroicons.Solid.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatRating(rating),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PassengerRouteTimeline(
    pickupLabel: String,
    destinationLabel: String,
    modifier: Modifier = Modifier,
    lineColor: Color,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            RouteTimelineDot(
                outerColor = MaterialTheme.colorScheme.primary,
                innerColor = MaterialTheme.colorScheme.onPrimary,
            )
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 42.dp)
                    .padding(vertical = 4.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = lineColor,
                ) {}
            }
            RouteTimelineDot(
                outerColor = MaterialTheme.colorScheme.errorContainer,
                innerColor = MaterialTheme.colorScheme.error,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TripPointText("Pickup", pickupLabel)
            TripPointText("Drop-off", destinationLabel)
        }
    }
}

@Composable
private fun RouteTimelineDot(
    outerColor: Color,
    innerColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = outerColor,
        modifier = Modifier.size(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = innerColor,
                modifier = Modifier.size(6.dp),
            ) {}
        }
    }
}
@Composable
private fun TripPointText(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun TripTrackingSession.tripMarkerCenter(): MapPoint? {
    val points = tripMarkerPoints()
    if (points.isEmpty()) return null

    val minLatitude = points.minOf { it.latitude }
    val maxLatitude = points.maxOf { it.latitude }
    val minLongitude = points.minOf { it.longitude }
    val maxLongitude = points.maxOf { it.longitude }

    return MapPoint(
        latitude = (minLatitude + maxLatitude) / 2.0,
        longitude = (minLongitude + maxLongitude) / 2.0,
    )
}

private fun TripTrackingSession.tripMarkerZoom(): Double {
    val points = tripMarkerPoints()
    if (points.size <= 1) return 15.0

    val maxDistanceKm = points.maxPairDistanceKm()
    return when {
        maxDistanceKm <= 0.4 -> 15.0
        maxDistanceKm <= 1.0 -> 14.2
        maxDistanceKm <= 2.0 -> 13.6
        maxDistanceKm <= 5.0 -> 12.8
        maxDistanceKm <= 10.0 -> 12.1
        maxDistanceKm <= 20.0 -> 11.4
        maxDistanceKm <= 50.0 -> 10.5
        else -> 9.4
    }
}

private fun TripTrackingSession.tripMarkerPoints(): List<MapPoint> = listOfNotNull(
    latestLocation?.toMapPoint(),
    pickupPoint.toMapPoint(),
    destinationPoint.toMapPoint(),
)

private fun List<MapPoint>.maxPairDistanceKm(): Double {
    var maxDistance = 0.0
    for (startIndex in indices) {
        for (endIndex in startIndex + 1 until size) {
            maxDistance = maxOf(maxDistance, this[startIndex].distanceToKm(this[endIndex]))
        }
    }
    return maxDistance
}

private fun MapPoint.distanceToKm(other: MapPoint): Double {
    val earthRadiusKm = 6371.0
    val latitudeDelta = (other.latitude - latitude).toRadians()
    val longitudeDelta = (other.longitude - longitude).toRadians()
    val startLatitude = latitude.toRadians()
    val endLatitude = other.latitude.toRadians()
    val haversine = sin(latitudeDelta / 2.0) * sin(latitudeDelta / 2.0) +
        cos(startLatitude) * cos(endLatitude) * sin(longitudeDelta / 2.0) * sin(longitudeDelta / 2.0)
    val centralAngle = 2.0 * atan2(sqrt(haversine), sqrt(1.0 - haversine))
    return earthRadiusKm * centralAngle
}

private fun Double.toRadians(): Double = this * PI / 180.0

private fun buildTripMarkers(
    session: TripTrackingSession,
    stage: TripTrackingStage,
): List<MapMarker> = buildList {
    session.latestLocation?.let { latestLocation ->
        add(
            MapMarker(
                id = "driver",
                point = latestLocation.toMapPoint(),
                color = Color(0xFF2563EB),
                radius = 8.0,
                icon = MapMarkerIcon.DriverLocation,
            ),
        )
    }
    session.pickupPoint.toMapPoint()?.let { pickupPoint ->
        val color = if (stage == TripTrackingStage.ToDropoff) Color(0xFF94A3B8) else Color(0xFFF59E0B)
        val radius = if (stage == TripTrackingStage.ToDropoff) 5.0 else 7.0
        add(
            MapMarker(
                id = "pickup",
                point = pickupPoint,
                color = color,
                radius = radius,
                icon = MapMarkerIcon.PickupPassenger,
            ),
        )
    }
    session.destinationPoint.toMapPoint()?.let { destinationPoint ->
        add(
            MapMarker(
                id = "destination",
                point = destinationPoint,
                color = Color(0xFFEF4444),
                radius = 7.0,
                icon = MapMarkerIcon.DestinationFlag,
            ),
        )
    }
}

private fun buildTripRouteLines(uiState: TripTrackingUIState): List<MapRouteLine> = buildList {
    when (uiState.stage) {
        TripTrackingStage.ToPickup -> {
            val hasActiveDriverRoute = uiState.riderToPickupRoute.size >= 2
            if (hasActiveDriverRoute) {
                add(MapRouteLine("driver-pickup-glow", uiState.riderToPickupRoute, Color(0xFF2563EB), 10.0, opacity = 0.22))
                add(MapRouteLine("driver-pickup-active-base", uiState.riderToPickupRoute, Color(0xFF2563EB), 5.5, opacity = 0.94))
                add(MapRouteLine("driver-pickup-active", uiState.riderToPickupRoute, Color(0xFF2563EB), 5.5, animatedAntPath = true))
                if (uiState.pickupToDestinationRoute.size >= 2) {
                    add(MapRouteLine("pickup-dropoff-preview", uiState.pickupToDestinationRoute, Color(0xFF64748B), 3.0, opacity = 0.48, dashPattern = listOf(1.0, 2.0)))
                }
            }
        }
        TripTrackingStage.ArrivedPickup -> {
            if (uiState.pickupToDestinationRoute.size >= 2) {
                add(MapRouteLine("pickup-dropoff-preview", uiState.pickupToDestinationRoute, Color(0xFF64748B), 4.0, opacity = 0.58, dashPattern = listOf(1.0, 2.0)))
            }
        }
        TripTrackingStage.ToDropoff -> {
            if (uiState.pickupToDestinationRoute.size >= 2) {
                add(MapRouteLine("completed-pickup-dropoff", uiState.pickupToDestinationRoute, Color(0xFFCBD5E1), 3.0, opacity = 0.62))
            }
            if (uiState.driverToDestinationRoute.size >= 2) {
                add(MapRouteLine("driver-dropoff-glow", uiState.driverToDestinationRoute, Color(0xFF2563EB), 10.0, opacity = 0.22))
                add(MapRouteLine("driver-dropoff-active-base", uiState.driverToDestinationRoute, Color(0xFF2563EB), 5.5, opacity = 0.94))
                add(MapRouteLine("driver-dropoff-active", uiState.driverToDestinationRoute, Color(0xFF2563EB), 5.5, animatedAntPath = true))
            }
        }
        TripTrackingStage.Completed -> {
            if (uiState.pickupToDestinationRoute.size >= 2) {
                add(MapRouteLine("completed-trip", uiState.pickupToDestinationRoute, Color(0xFF10B981), 4.0))
            }
        }
    }
}

private fun TripTrackingSession.fareLabel(): String? {
    val amount = finalFare ?: return null
    val currencyCode = currency ?: "PHP"
    val cents = (amount * 100).roundToInt().coerceAtLeast(0)
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    val prefix = when (currencyCode.uppercase()) {
        "PHP" -> "₱"
        "USD" -> "\$"
        else -> "${currencyCode.uppercase()} "
    }
    return "$prefix$whole.$fraction"
}

private fun sessionStatusLabel(stage: TripTrackingStage): String = when (stage) {
    TripTrackingStage.ToPickup -> "Driver is on the way"
    TripTrackingStage.ArrivedPickup -> "Driver has arrived"
    TripTrackingStage.ToDropoff -> "On the way to destination"
    TripTrackingStage.Completed -> "Trip completed"
}

private fun formatRating(rating: Double): String {
    val rounded = (rating.coerceIn(0.0, 5.0) * 10).roundToInt() / 10.0
    return rounded.toString()
}

private fun LatestLocation.toMapPoint(): MapPoint {
    return MapPoint(latitude, longitude)
}

private fun TripPoint.toMapPoint(): MapPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return MapPoint(lat, lng)
}
