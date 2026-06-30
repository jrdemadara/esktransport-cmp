package org.noztek.esktransport.feature.driver.trip_navigation.presentation

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Check
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.HoldToCancelButton
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripNavigationScreen(
    bookingPublicId: String,
    onCancelled: () -> Unit,
    viewModel: TripNavigationViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingPublicId) {
        viewModel.load(bookingPublicId)
    }

    DisposableEffect(viewModel, bookingPublicId) {
        viewModel.startRealtime(bookingPublicId)
        onDispose { viewModel.stopRealtime() }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                TripNavigationUiEvent.NavigateToGoScreen -> onCancelled()
            }
        }
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
                val isNearPickup = (uiState.distanceMeters ?: Double.MAX_VALUE) <= 30.0
                val showConfirmPickup = session.phase == RiderTripPhase.TO_PICKUP && isNearPickup
                BottomSheetScaffold(
                    sheetPeekHeight = 70.dp,
                    sheetShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    sheetContainerColor = MaterialTheme.colorScheme.background,
                    sheetDragHandle = null,
                    sheetContent = {
                        TripNavigationBottomSheet(
                            passengerName = session.passengerName,
                            phase = session.phase,
                            pickupLabel = session.pickupLabel,
                            destinationLabel = session.destinationLabel,
                            durationLabel = formatDuration(uiState.durationSeconds),
                            distanceLabel = formatDistance(uiState.distanceMeters),
                            isCancelling = uiState.isCancelling,
                            onCancel = { viewModel.cancelTrip(bookingPublicId) },
                        )
                    },
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DriverTurnByTurnHost(
                            modifier = Modifier.fillMaxSize(),
                            mapboxConfig = mapboxConfig,
                            pickupPoint = pickup,
                            destinationPoint = destination,
                            routePoints = uiState.routePoints,
                            pickupConfirmed = session.phase != RiderTripPhase.TO_PICKUP,
                            onLocationChanged = { location ->
                                viewModel.publishLocationIfNeeded(
                                    bookingPublicId = bookingPublicId,
                                    location = location,
                                )
                            },
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 110.dp)
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

                        FloatingActionButton(
                            onClick = {},
                            containerColor = Color.White,
                            contentColor = Color(0xFF2B6EF2),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 110.dp)
                                .size(48.dp),
                        ) { Icon(Heroicons.Outline.ShieldCheck, contentDescription = "Safety") }

                        if (showConfirmPickup) {
                            FloatingActionButton(
                                onClick = { viewModel.confirmPickup(bookingPublicId) },
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 110.dp)
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
}

@Composable
private fun TripNavigationBottomSheet(
    passengerName: String,
    phase: RiderTripPhase,
    pickupLabel: String,
    destinationLabel: String,
    durationLabel: String,
    distanceLabel: String,
    isCancelling: Boolean,
    onCancel: () -> Unit,
) {
    val isPickupPhase = phase == RiderTripPhase.TO_PICKUP
    val currentStageTitle = if (isPickupPhase) "Pickup $passengerName" else "Dropoff $passengerName"
    val currentStageMetric = "$durationLabel • $distanceLabel"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StageRow(
            currentStageTitle = currentStageTitle,
            currentStageMetric = currentStageMetric,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Trip stages",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Follow the route in order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        TripStageTimeline(
            pickupTitle = "Pickup $passengerName",
            pickupLabel = pickupLabel,
            pickupMetric = if (isPickupPhase) currentStageMetric else "Completed",
            destinationTitle = "Dropoff $passengerName",
            destinationLabel = destinationLabel,
            destinationMetric = if (isPickupPhase) "Pending" else currentStageMetric,
            isPickupPhase = isPickupPhase,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "The passenger will be notified.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HoldToCancelButton(
            isCancelling = isCancelling,
            onCancel = onCancel,
            text = "Hold 3s to cancel trip",
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun StageRow(
    currentStageTitle: String,
    currentStageMetric: String,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
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
                        .background(primaryColor),
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                )
            }

            Text(
                text = currentStageTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.25).sp,
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                text = currentStageMetric,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = (-0.15).sp,
            )
        }
    }
}

@Composable
private fun TripStageTimeline(
    pickupTitle: String,
    pickupLabel: String,
    pickupMetric: String,
    destinationTitle: String,
    destinationLabel: String,
    destinationMetric: String,
    isPickupPhase: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp),
        ) {
            StageDot(active = isPickupPhase)
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 46.dp)
                    .padding(vertical = 4.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outlineVariant,
                ) {}
            }
            StageDot(active = !isPickupPhase)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TripStageRow(
                title = pickupTitle,
                label = pickupLabel,
                metric = pickupMetric,
                active = isPickupPhase,
            )
            TripStageRow(
                title = destinationTitle,
                label = destinationLabel,
                metric = destinationMetric,
                active = !isPickupPhase,
            )
        }
    }
}

@Composable
private fun StageDot(active: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(6.dp),
            ) {}
        }
    }
}

@Composable
private fun TripStageRow(
    title: String,
    label: String,
    metric: String,
    active: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                metric,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
