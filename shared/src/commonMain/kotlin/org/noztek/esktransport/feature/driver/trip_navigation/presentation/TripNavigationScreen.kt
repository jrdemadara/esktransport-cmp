package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView

@Composable
fun TripNavigationScreen(
    bookingPublicId: String,
    viewModel: TripNavigationViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig,
) {
    val uiState by viewModel.uiState.collectAsState()
    var pickupConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(bookingPublicId) {
        viewModel.load(bookingPublicId)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Trip Navigation", style = MaterialTheme.typography.titleMedium)
                    CircularProgressIndicator()
                }
            }

            uiState.tripSession == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Trip Navigation", style = MaterialTheme.typography.titleMedium)
                    Text(uiState.message ?: "Trip session unavailable.")
                }
            }

            else -> {
                val session = uiState.tripSession ?: return@Surface
                val pickup = MapPoint(session.pickupPoint.latitude, session.pickupPoint.longitude)
                val destination = MapPoint(session.destinationPoint.latitude, session.destinationPoint.longitude)
                val center = if (pickupConfirmed) destination else pickup

                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        tonalElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(uiState.nextInstruction ?: "Continue on route", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Remaining: ${formatDistance(uiState.distanceMeters)} • ${formatDuration(uiState.durationSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (!pickupConfirmed) {
                                Button(onClick = { pickupConfirmed = true }) {
                                    Text("Confirm Pickup")
                                }
                            }
                        }
                    }
                    PlatformMapView(
                        modifier = Modifier.fillMaxSize(),
                        config = mapboxConfig,
                        cameraCenter = center,
                        cameraDefaults = MapCameraDefaults(zoom = 13.2, pitch = 30.0),
                        routeLines = listOf(
                            MapRouteLine(
                                id = "trip-route",
                                points = uiState.routePoints.map { point ->
                                    MapPoint(point.latitude, point.longitude)
                                },
                            ),
                        ),
                    )
                }
            }
        }
    }
}

private fun formatDistance(distanceMeters: Double?): String {
    val value = distanceMeters ?: return "N/A"
    return if (value >= 1000.0) {
        val km = value / 1000.0
        val rounded = ((km * 10).toInt() / 10.0)
        if (rounded % 1.0 == 0.0) "${rounded.toInt()} km" else "$rounded km"
    } else {
        "${value.toInt()} m"
    }
}

private fun formatDuration(durationSeconds: Double?): String {
    val value = durationSeconds ?: return "N/A"
    val minutes = (value / 60.0).toInt()
    return when {
        minutes < 1 -> "<1 min"
        minutes < 60 -> "$minutes min"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
