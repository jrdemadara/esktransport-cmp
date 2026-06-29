package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
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
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.LatestLocation
import org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripPoint
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTrackingScreen(
    bookingId: String,
    onCancelled: () -> Unit,
    viewModel: TripTrackingViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.tripSession

    LaunchedEffect(bookingId) { viewModel.loadTripData(bookingId) }

    DisposableEffect(viewModel, bookingId) {
        viewModel.startRealtime(bookingId)
        onDispose { viewModel.stopRealtime() }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                TripTrackingUiEvent.NavigateToBookingReview -> onCancelled()
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
                    vehicleLabel = "${it.riderInfo.vehicleLabel} - ${it.riderInfo.vehiclePlate}",
                    pickupLabel = it.pickupPoint.label,
                    destinationLabel = it.destinationPoint.label,
                    stage = uiState.stage,
                    isCancelling = uiState.isCancelling,
                    onCancel = { viewModel.cancelTrip(bookingId) },
                )
            } ?: LoadingSheet(isLoading = uiState.isLoading)
        },
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            val center = session?.latestLocation?.toMapPoint()
                ?: session?.pickupPoint?.toMapPoint()
                ?: session?.destinationPoint?.toMapPoint()
            val markers = session?.let { buildTripMarkers(it, uiState.stage) }.orEmpty()
            val routeLines = buildTripRouteLines(uiState)
            if (center != null) {
                PlatformMapView(
                    modifier = Modifier.fillMaxSize(),
                    config = mapboxConfig,
                    cameraCenter = center,
                    cameraDefaults = MapCameraDefaults(zoom = 14.0, pitch = 30.0),
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
        }
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
    vehicleLabel: String,
    pickupLabel: String,
    destinationLabel: String,
    stage: TripTrackingStage,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusLabel(stage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
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
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ContactActionButton(
                    icon = {
                        Icon(
                            Heroicons.Outline.ChatBubbleOvalLeft,
                            contentDescription = "Chat",
                            modifier = Modifier.size(21.dp),
                        )
                    },
                    onClick = {},
                )
                ContactActionButton(
                    icon = {
                        Icon(
                            Heroicons.Outline.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(21.dp),
                        )
                    },
                    onClick = {},
                )
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

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RouteTimeline(
                modifier = Modifier.padding(top = 2.dp),
                lineColor = MaterialTheme.colorScheme.outlineVariant,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TripPointText("Pickup", pickupLabel)
                TripPointText("Drop-off", destinationLabel)
            }
        }

        Text(
            text = stageHint(stage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HoldToCancelButton(
            isCancelling = isCancelling,
            onCancel = onCancel,
            modifier = Modifier.fillMaxWidth(),
            text = "Hold to cancel trip",
        )
    }
}

@Composable
private fun ContactActionButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        icon()
    }
}

@Composable
private fun RiderRatingLabel(rating: Double) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                Heroicons.Solid.Star,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = formatRating(rating),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RouteTimeline(
    modifier: Modifier = Modifier,
    lineColor: Color,
) {
    Column(
        modifier = modifier.width(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(58.dp)
                .background(lineColor, RoundedCornerShape(999.dp)),
        )
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape),
        )
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

private fun buildTripMarkers(
    session: org.noztek.esktransport.feature.passenger.trip_tracking.domain.model.TripTrackingSession,
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

private fun statusLabel(stage: TripTrackingStage): String = when (stage) {
    TripTrackingStage.ToPickup -> "Driver is on the way"
    TripTrackingStage.ArrivedPickup -> "Driver has arrived"
    TripTrackingStage.ToDropoff -> "Heading to destination"
    TripTrackingStage.Completed -> "Trip completed"
}

private fun stageHint(stage: TripTrackingStage): String = when (stage) {
    TripTrackingStage.ToPickup -> "Track your driver to the pickup point."
    TripTrackingStage.ArrivedPickup -> "Meet your driver at the pickup point."
    TripTrackingStage.ToDropoff -> "Follow the trip to your destination."
    TripTrackingStage.Completed -> "Your trip is complete."
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
