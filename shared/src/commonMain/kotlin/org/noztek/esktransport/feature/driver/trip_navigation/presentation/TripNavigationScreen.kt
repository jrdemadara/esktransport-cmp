package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Check
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase

@Composable
fun TripNavigationScreen(
    bookingPublicId: String,
    viewModel: TripNavigationViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig,
) {
    val uiState by viewModel.uiState.collectAsState()

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
                val showConfirmPickup = session.phase == RiderTripPhase.TO_PICKUP
                Box(modifier = Modifier.fillMaxSize()) {
                    DriverTurnByTurnHost(
                        modifier = Modifier.fillMaxSize(),
                        mapboxConfig = mapboxConfig,
                        pickupPoint = pickup,
                        destinationPoint = destination,
                        routePoints = uiState.routePoints,
                        pickupConfirmed = session.phase != RiderTripPhase.TO_PICKUP,
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 94.dp)
                            .offset(y = (-8).dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            modifier = Modifier.size(48.dp),
                        ) { Icon(Heroicons.Outline.MapPin, contentDescription = "Pin") }
                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            modifier = Modifier.size(48.dp),
                        ) { Text("↕") }
                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            modifier = Modifier.size(48.dp),
                        ) { Icon(Heroicons.Outline.ChatBubbleOvalLeft, contentDescription = "Chat") }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            val stageTitle = if (session.phase == RiderTripPhase.TO_PICKUP) {
                                "Pickup ${session.passengerName}"
                            } else {
                                "Dropoff ${session.passengerName}"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    modifier = Modifier.size(12.dp),
                                ) {
                                    Box {}
                                }
                                Text(
                                    stageTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Text(
                                "ETA ${formatDuration(uiState.durationSeconds)} • ${formatDistance(uiState.distanceMeters)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = {},
                        containerColor = Color.White,
                        contentColor = Color(0xFF2B6EF2),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 94.dp)
                            .size(48.dp),
                    ) { Icon(Heroicons.Outline.ShieldCheck, contentDescription = "Safety") }

                    if (showConfirmPickup) {
                        FloatingActionButton(
                            onClick = { viewModel.confirmPickup(bookingPublicId) },
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 94.dp)
                                .widthIn(min = 150.dp)
                                .size(height = 56.dp, width = 150.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Heroicons.Outline.Check,
                                    contentDescription = null,
                                )
                                Text(
                                    if (uiState.isSubmittingPickup) "..." else "Confirm Pickup",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }

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
