package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Check
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
                            onCancel = {},
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
        SwipeToCancelButton(
            onCancel = onCancel,
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
private fun SwipeToCancelButton(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Apple System Red (#FF3B30)
    val cancelRed = Color(0xFFFF3B30)
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    // Smooth breathing animation for the swipe instruction text
    val infiniteTransition = rememberInfiniteTransition(label = "CancelTextPulse")
    val textPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.52f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cancelTextPulseAlpha"
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CircleShape)
            // Premium semi-translucent iOS red container track
            .background(cancelRed.copy(alpha = 0.08f))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart // Start handle on the left side
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val handleSizePx = with(density) { 46.dp.toPx() }
        val paddingPx = with(density) { 4.dp.toPx() }

        // Max distance the handle can travel to the right
        val maxDragDistance = (containerWidthPx - handleSizePx - paddingPx * 2f).coerceAtLeast(0f)
        val dragOffset = remember { Animatable(0f) }
        var isTriggered by remember { mutableStateOf(false) }
        // Track how far the handle has been swiped to fade out the label
        val dragFraction = if (maxDragDistance > 0f) (dragOffset.value / maxDragDistance).coerceIn(0f, 1f) else 0f
        val textAlpha = ((1f - dragFraction * 2.2f) * textPulseAlpha).coerceIn(0f, 1f)
        // Centered instruction text inside the track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 52.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "Swipe right to cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = cancelRed.copy(alpha = 0.85f),
                    letterSpacing = (-0.2).sp
                )
                // Chevron pointing to the right
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = cancelRed.copy(alpha = 0.85f)
                )
            }
        }
        // Swipe Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                .size(46.dp)
                .clip(CircleShape)
                .background(cancelRed)
                .pointerInput(maxDragDistance) {
                    detectDragGestures(
                        onDragEnd = {
                            if (isTriggered) return@detectDragGestures

                            val threshold = maxDragDistance * 0.80f
                            if (dragOffset.value >= threshold) {
                                isTriggered = true
                                coroutineScope.launch {
                                    // Core iOS confirmation feel (medium/long press vibration)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // Complete lock-in to the right edge
                                    dragOffset.animateTo(maxDragDistance, spring(stiffness = Spring.StiffnessMedium))
                                    onCancel()
                                }
                            } else {
                                coroutineScope.launch {
                                    // Rebounds with standard iOS spring feel if released early
                                    dragOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium))
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium))
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isTriggered) return@detectDragGestures
                            val originalValue = dragOffset.value
                            val newValue = (originalValue + dragAmount.x).coerceIn(0f, maxDragDistance)
                            coroutineScope.launch {
                                dragOffset.snapTo(newValue)
                            }
                            // Tactile "tick" when user reaches the trigger threshold
                            val threshold = maxDragDistance * 0.80f
                            if (newValue >= threshold && originalValue < threshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Elegant white chevron vector pointing right
            Canvas(modifier = Modifier.size(16.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.38f, size.height * 0.22f)
                    lineTo(size.width * 0.62f, size.height * 0.50f)
                    lineTo(size.width * 0.38f, size.height * 0.78f)
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
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
