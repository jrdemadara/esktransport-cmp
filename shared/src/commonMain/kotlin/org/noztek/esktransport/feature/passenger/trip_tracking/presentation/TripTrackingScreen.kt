package org.noztek.esktransport.feature.passenger.trip_tracking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTrackingScreen(
    bookingId: String,
    viewModel: TripTrackingViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val session = uiState.tripSession

    LaunchedEffect(bookingId) { viewModel.loadTripData(bookingId) }

    BottomSheetScaffold(
        sheetPeekHeight = 220.dp,
        sheetContent = {
            session?.let {
                TripTrackingSheet(
                    riderName = it.riderInfo.name,
                    vehicleLabel = "${it.riderInfo.vehicleLabel} - ${it.riderInfo.vehiclePlate}",
                    pickupLabel = it.pickupPoint.label,
                    destinationLabel = it.destinationPoint.label,
                    status = it.status,
                )
            } ?: LoadingSheet(isLoading = uiState.isLoading)
        },
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            val center = session?.latestLocation?.let { MapPoint(it.latitude, it.longitude) }
                ?: MapPoint(6.6920431660391095, 124.68050838312321)
            PlatformMapView(
                modifier = Modifier.fillMaxSize(),
                config = mapboxConfig,
                cameraCenter = center,
                cameraDefaults = MapCameraDefaults(zoom = 14.0, pitch = 30.0),
                markers = session?.let {
                    listOf(
                        MapMarker("driver", MapPoint(it.latestLocation.latitude, it.latestLocation.longitude), Color(0xFF2563EB), 8.0),
                        MapMarker("pickup", MapPoint(it.pickupPoint.latitude, it.pickupPoint.longitude), Color(0xFFF59E0B), 7.0),
                        MapMarker("destination", MapPoint(it.destinationPoint.latitude, it.destinationPoint.longitude), Color(0xFFEF4444), 7.0),
                    )
                }.orEmpty(),
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
    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
        if (isLoading) CircularProgressIndicator() else Text("Loading trip details...")
    }
}

@Composable
private fun TripTrackingSheet(
    riderName: String,
    vehicleLabel: String,
    pickupLabel: String,
    destinationLabel: String,
    status: String,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Heroicons.Outline.User, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(riderName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(vehicleLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = {},
                modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(Heroicons.Outline.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        TripPointRow("Pickup", pickupLabel, MaterialTheme.colorScheme.primary)
        TripPointRow("Destination", destinationLabel, Color(0xFFEF4444))
        Text(
            text = "Status: ${status.replace("_", " ").uppercase()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Cancel Booking") }
    }
}

@Composable
private fun TripPointRow(label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Heroicons.Outline.MapPin, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
