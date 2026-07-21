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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.outline.Check
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.solid.User
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.common.HoldToCancelButton
import org.noztek.esktransport.core.ui.composables.common.TripFeedbackDialog
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripPhase
import org.noztek.esktransport.feature.rider.trip_navigation.domain.model.RiderTripSession
import kotlin.math.roundToInt

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
                val showConfirmPickup = session.phase == RiderTripPhase.TO_PICKUP
                val showCompleteTrip = session.phase == RiderTripPhase.TO_DESTINATION
                var showPickupConfirmDialog by rememberSaveable(bookingPublicId) { mutableStateOf(false) }
                var showCompleteTripDialog by rememberSaveable(bookingPublicId) { mutableStateOf(false) }
                BottomSheetScaffold(
                    sheetPeekHeight = 64.dp,
                    sheetShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    sheetContainerColor = MaterialTheme.colorScheme.background,
                    sheetDragHandle = null,
                    sheetContent = {
                        TripNavigationBottomSheet(
                            passengerName = session.passengerName,
                            fareLabel = session.fareLabel(),
                            phase = session.phase,
                            pickupLabel = session.pickupLabel,
                            destinationLabel = session.destinationLabel,
                            durationLabel = formatDuration(uiState.durationSeconds),
                            distanceLabel = formatDistance(uiState.distanceMeters),
                            isCancelling = uiState.isCancelling,
                            showConfirmPickup = showConfirmPickup,
                            isSubmittingPickup = uiState.isSubmittingPickup,
                            onConfirmPickup = { showPickupConfirmDialog = true },
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

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 110.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FloatingActionButton(
                                onClick = {},
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(Heroicons.Outline.ChatBubbleOvalLeft, contentDescription = "Chat")
                            }
                            FloatingActionButton(
                                onClick = {},
                                containerColor = Color.White,
                                contentColor = Color(0xFF2B6EF2),
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(Heroicons.Outline.ShieldCheck, contentDescription = "Safety")
                            }
                        }

//                        if (showCompleteTrip) {
//                            FloatingActionButton(
//                                onClick = { showCompleteTripDialog = true },
//                                containerColor = MaterialTheme.colorScheme.primary,
//                                contentColor = MaterialTheme.colorScheme.onPrimary,
//                                modifier = Modifier
//                                    .align(Alignment.BottomEnd)
//                                    .padding(end = 16.dp, bottom = 110.dp)
//                                    .widthIn(min = 150.dp)
//                                    .size(height = 56.dp, width = 150.dp),
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                                ) {
//                                    Icon(
//                                        imageVector = Heroicons.Outline.Check,
//                                        contentDescription = null,
//                                    )
//                                    Text(
//                                        if (uiState.isCompletingTrip) "..." else "Complete Trip",
//                                        style = MaterialTheme.typography.labelMedium,
//                                    )
//                                }
//                            }
//                        }

                        if (showPickupConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    if (!uiState.isSubmittingPickup) showPickupConfirmDialog = false
                                },
                                title = { Text("Confirm pickup") },
                                text = { Text("Is the passenger already onboard? This will start navigation to the destination.") },
                                confirmButton = {
                                    TextButton(
                                        enabled = !uiState.isSubmittingPickup,
                                        onClick = {
                                            showPickupConfirmDialog = false
                                            viewModel.confirmPickup(bookingPublicId)
                                        },
                                    ) {
                                        Text("Start dropoff")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        enabled = !uiState.isSubmittingPickup,
                                        onClick = { showPickupConfirmDialog = false },
                                    ) {
                                        Text("Not yet")
                                    }
                                },
                            )
                        }

                        if (showCompleteTripDialog) {
                            AlertDialog(
                                onDismissRequest = {
                                    if (!uiState.isCompletingTrip) showCompleteTripDialog = false
                                },
                                title = { Text("Complete trip") },
                                text = { Text("Confirm that the passenger has paid in cash and the trip is complete.") },
                                confirmButton = {
                                    TextButton(
                                        enabled = !uiState.isCompletingTrip,
                                        onClick = {
                                            showCompleteTripDialog = false
                                            viewModel.completeTrip(bookingPublicId)
                                        },
                                    ) {
                                        Text("Complete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        enabled = !uiState.isCompletingTrip,
                                        onClick = { showCompleteTripDialog = false },
                                    ) {
                                        Text("Not yet")
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showFeedback) {
        TripFeedbackDialog(
            title = "Rate the passenger",
            message = "Trip completed. Add a quick rating for this passenger.",
            isSubmitting = uiState.isSubmittingFeedback,
            onSubmit = { rating, comment ->
                viewModel.submitFeedback(
                    bookingPublicId = bookingPublicId,
                    rating = rating,
                    comment = comment,
                )
            },
            onDismiss = viewModel::skipFeedback,
        )
    }
}

@Composable
private fun TripNavigationBottomSheet(
    passengerName: String,
    fareLabel: String?,
    phase: RiderTripPhase,
    pickupLabel: String,
    destinationLabel: String,
    durationLabel: String,
    distanceLabel: String,
    isCancelling: Boolean,
    showConfirmPickup: Boolean,
    isSubmittingPickup: Boolean,
    onConfirmPickup: () -> Unit,
    onCancel: () -> Unit,
) {
    val isPickupPhase = phase == RiderTripPhase.TO_PICKUP
    val currentStageTitle = if (isPickupPhase) "Pickup $passengerName" else "Dropoff $passengerName"
    val currentStageMetric = "$durationLabel • $distanceLabel"
    val stageSummary = if (isPickupPhase) "Picking up $passengerName" else "Dropping off $passengerName"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NavigationPeekRow(
            durationLabel = durationLabel,
            distanceLabel = distanceLabel,
            stageSummary = stageSummary,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

        StageRow(
            currentStageTitle = currentStageTitle,
            fareLabel = fareLabel,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

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

        if (showConfirmPickup) {
            AppPrimaryButton(
                text = if (isSubmittingPickup) "Confirming..." else "Confirm Pickup",
                onClick = onConfirmPickup,
                enabled = !isSubmittingPickup,
                height = 52.dp,
                trailingIcon = {
                    Icon(
                        imageVector = Heroicons.Outline.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

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
private fun NavigationPeekRow(
    durationLabel: String,
    distanceLabel: String,
    stageSummary: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Heroicons.Outline.AdjustmentsHorizontal,
            contentDescription = "Route options",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = durationLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    modifier = Modifier.size(18.dp),
                    shape = CircleShape,
                    color = Color(0xFF22A663),
                    contentColor = Color.White,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Solid.User,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                        )
                    }
                }
                Text(
                    text = distanceLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = stageSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        Icon(
            imageVector = Heroicons.Outline.QueueList,
            contentDescription = "Trip steps",
            modifier = Modifier.size(23.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun StageRow(
    currentStageTitle: String,
    fareLabel: String?,
    modifier: Modifier = Modifier,
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

    Row(
        modifier = modifier
            .fillMaxWidth(),
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
                text = currentStageTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.25).sp,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = fareLabel ?: "Fare pending",
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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

private fun RiderTripSession.fareLabel(): String? {
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
