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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.User
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.core.ui.composables.common.HoldToCancelButton

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

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                TripTrackingUiEvent.NavigateToBookingReview -> onCancelled()
            }
        }
    }

    BottomSheetScaffold(
        sheetPeekHeight = 196.dp,
        sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetContent = {
            session?.let {
                TripTrackingSheet(
                    riderName = it.riderInfo.name,
                    vehicleLabel = "${it.riderInfo.vehicleLabel} - ${it.riderInfo.vehiclePlate}",
                    pickupLabel = it.pickupPoint.label,
                    destinationLabel = it.destinationPoint.label,
                    status = it.status,
                    isCancelling = uiState.isCancelling,
                    onCancel = { viewModel.cancelTrip(bookingId) },
                )
            } ?: LoadingSheet(isLoading = uiState.isLoading)
        },
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            val center = session?.latestLocation?.let { MapPoint(it.latitude, it.longitude) }
                ?: session?.pickupPoint?.let { MapPoint(it.latitude, it.longitude) }
                ?: MapPoint(6.6920431660391095, 124.68050838312321)
            val markers = session?.let { tripSession ->
                buildList {
                    tripSession.latestLocation?.let { latestLocation ->
                        add(MapMarker("driver", MapPoint(latestLocation.latitude, latestLocation.longitude), Color(0xFF2563EB), 8.0))
                    }
                    add(MapMarker("pickup", MapPoint(tripSession.pickupPoint.latitude, tripSession.pickupPoint.longitude), Color(0xFFF59E0B), 7.0))
                    add(MapMarker("destination", MapPoint(tripSession.destinationPoint.latitude, tripSession.destinationPoint.longitude), Color(0xFFEF4444), 7.0))
                }
            }.orEmpty()
            PlatformMapView(
                modifier = Modifier.fillMaxSize(),
                config = mapboxConfig,
                cameraCenter = center,
                cameraDefaults = MapCameraDefaults(zoom = 14.0, pitch = 30.0),
                markers = markers,
                routeLines = listOf(
                    MapRouteLine("driver-pickup", uiState.riderToPickupRoute, Color(0xFF2563EB), 5.0),
                    MapRouteLine("pickup-destination", uiState.pickupToDestinationRoute, Color(0xFF10B981), 5.0),
                ),
            )

            session?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(statusLabel(it.status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Driver: ${it.riderInfo.name} - ${it.riderInfo.vehiclePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
    vehicleLabel: String,
    pickupLabel: String,
    destinationLabel: String,
    status: String,
    isCancelling: Boolean,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusLabel(status),
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
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(
                    Heroicons.Outline.Phone,
                    contentDescription = "Call",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Text(
            text = vehicleLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TripPointRow("Pickup", pickupLabel, MaterialTheme.colorScheme.primary)
        TripPointRow("Destination", destinationLabel, MaterialTheme.colorScheme.error)
        Text(
            text = status.replace("_", " ").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        Text(
            text = "Your driver will be notified.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HoldToCancelButton(
            isCancelling = isCancelling,
            onCancel = onCancel,
            text = "Hold 3s to cancel ride",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TripPointRow(label: String, value: String, iconColor: Color) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            Heroicons.Outline.MapPin,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = iconColor,
        )
        Column(modifier = Modifier.weight(1f)) {
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
}

private fun statusLabel(status: String): String = when (status.lowercase()) {
    "accepted" -> "Driver is coming to you"
    "arrived" -> "Driver has arrived"
    "in_progress" -> "Heading to destination"
    "completed" -> "Trip completed"
    else -> status.replace("_", " ")
}
