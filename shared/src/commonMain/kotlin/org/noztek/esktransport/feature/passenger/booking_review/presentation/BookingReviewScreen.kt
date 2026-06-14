package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.outline.Map
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Users
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.platform.isIosPlatform
import org.noztek.esktransport.core.ui.composables.AppPrimaryButton
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingReviewScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    input: BookingReviewInput,
    viewModel: BookingReviewViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    LaunchedEffect(input) { viewModel.setInput(input) }

    val uiState by viewModel.uiState.collectAsState()
    val stateInput = input
    val pickupPoint = stateInput.pickupPoint
    val destinationPoint = stateInput.destinationPoint
    val routePoints = stateInput.routePoints
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.startRealtime()
        viewModel.uiEvents.collect { event ->
            when (event) {
                is BookingReviewUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is BookingReviewUiEvent.NavigateToTripTracking -> Unit
            }
        }
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.stopRealtime() }
    }

    val tripDistanceLabel = remember(pickupPoint, destinationPoint) {
        formatDistanceLabel(
            haversineKm(
                lat1 = pickupPoint.latitude,
                lng1 = pickupPoint.longitude,
                lat2 = destinationPoint.latitude,
                lng2 = destinationPoint.longitude,
            ) * 1000.0,
        )
    }
    val vehicleLabel = when (stateInput.vehicleTypeIndex) {
        0 -> "MOTORCYCLE"
        1 -> "TRICYCLE"
        2 -> "CAR"
        3 -> "VAN"
        else -> "MOTORCYCLE"
    }
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = if (isIosPlatform()) SheetValue.Expanded else SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        ),
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = Color.Transparent,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = 252.dp,
        sheetContent = {
            when {
                uiState.isSearchExpired -> SearchExpiredSheet(
                    onTryAgain = viewModel::retryExpiredSearch,
                )
                uiState.isSearchingForRider -> SearchingSheet(
                    vehicleLabel = vehicleLabel,
                    seatCount = stateInput.requiredSeats,
                    distanceLabel = tripDistanceLabel,
                    destinationLocation = stateInput.destinationLocation,
                    secondsRemaining = uiState.searchSecondsRemaining,
                    isCancelling = uiState.isCancellingBooking,
                    onCancel = viewModel::cancelSearch,
                )
                else -> ReviewSheet(
                    pickupLocation = stateInput.pickupLocation,
                    destinationLocation = stateInput.destinationLocation,
                    vehicleLabel = vehicleLabel,
                    seatCount = stateInput.requiredSeats,
                    distanceLabel = tripDistanceLabel,
                    isCreatingBooking = uiState.isCreatingBooking,
                    onConfirm = viewModel::confirmBooking,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding),
        ) {
            BookingReviewMap(
                modifier = Modifier.fillMaxSize(),
                mapboxConfig = mapboxConfig,
                pickupPoint = pickupPoint,
                destinationPoint = destinationPoint,
                routePoints = routePoints,
            )
        }
    }
}

@Composable
private fun SearchingSheet(
    vehicleLabel: String,
    seatCount: Int,
    distanceLabel: String,
    destinationLocation: String,
    secondsRemaining: Int,
    isCancelling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val infiniteTransition = rememberInfiniteTransition(label = "SearchingAnimations")

    val progressTranslation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.MagnifyingGlass,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = primaryColor,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Finding your driver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor,
                        letterSpacing = (-0.3).sp // Apple-style tight tracking
                    )
                    Text(
                        text = "Request sent. Matching nearby online drivers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor,
                        lineHeight = 14.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = primaryColor.copy(alpha = 0.10f),
                ) {
                    Text(
                        text = formatSearchCountdown(secondsRemaining),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                }
            }
            Text(
                text = "$vehicleLabel • $seatCount ${if (seatCount == 1) "seat" else "seats"} • $distanceLabel",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
            )
            Text(
                text = "To: $destinationLocation",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                maxLines = 1,
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.08f))
            ) {
                val width = maxWidth
                val barWidth = width * 0.35f
                val offset = (width + barWidth) * progressTranslation - barWidth
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight()
                        .offset(x = offset)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            HoldToCancelButton(
                isCancelling = isCancelling,
                onCancel = onCancel,
            )
        }
    }
}

@Composable
private fun SearchExpiredSheet(
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "No drivers found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "No online driver accepted within 60 seconds. Try again when you are ready.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                )
            }
            AppPrimaryButton(
                text = "Try Again",
                onClick = onTryAgain,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun HoldToCancelButton(
    isCancelling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val errorColor = MaterialTheme.colorScheme.error
    val onErrorColor = MaterialTheme.colorScheme.onError
    val errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(errorContainerColor)
            .pointerInput(isCancelling, onCancel) {
                if (isCancelling) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                            .changes
                            .firstOrNull { it.pressed }
                        if (down == null) continue

                        val holdJob = scope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
                            )
                            onCancel()
                        }

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val stillPressed = event.changes.any { it.pressed }
                            if (!stillPressed) {
                                holdJob.cancel()
                                scope.launch { progress.animateTo(0f, tween(durationMillis = 160)) }
                                break
                            }
                        } while (true)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(maxWidth * progress.value)
                .background(errorColor),
        )
        Text(
            text = if (isCancelling) "Cancelling..." else "Press and hold 3s to cancel",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (progress.value > 0.55f) onErrorColor else errorColor,
        )
    }
}

@Composable
private fun ReviewSheet(
    pickupLocation: String,
    destinationLocation: String,
    vehicleLabel: String,
    seatCount: Int,
    distanceLabel: String,
    isCreatingBooking: Boolean,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Review ride", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Confirm the details before proceeding.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            ) {
                Text(
                    vehicleLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        TripPointRow("Pickup", pickupLocation, MaterialTheme.colorScheme.primary)
        TripPointRow("Destination", destinationLocation, Color(0xFFEF4444))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RideMetaPill(
                icon = { Icon(Heroicons.Outline.Users, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = "$seatCount seats",
                modifier = Modifier.weight(1f),
            )
            RideMetaPill(
                icon = { Icon(Heroicons.Outline.Map, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = distanceLabel,
                modifier = Modifier.weight(1f),
            )
        }
        AppPrimaryButton(
            text = if (isCreatingBooking) "Confirming..." else "Confirm Booking",
            onClick = onConfirm,
            enabled = !isCreatingBooking,
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun formatSearchCountdown(secondsRemaining: Int): String {
    val safeSeconds = secondsRemaining.coerceIn(0, 60)
    return "0:${safeSeconds.toString().padStart(2, '0')}"
}

@Composable
private fun TripPointRow(label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Heroicons.Outline.MapPin, contentDescription = null, modifier = Modifier.size(22.dp), tint = iconColor)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}

@Composable
private fun RideMetaPill(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF7F8FA),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            icon()
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun maxZoomForDistanceKm(distanceKm: Double): Double = when {
    distanceKm <= 0.5 -> 15.5
    distanceKm <= 1.5 -> 14.8
    distanceKm <= 3.0 -> 14.2
    distanceKm <= 6.0 -> 13.5
    distanceKm <= 10.0 -> 12.8
    distanceKm <= 20.0 -> 12.0
    else -> 11.2
}

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = kotlin.math.PI / 180.0 * (lat2 - lat1)
    val dLng = kotlin.math.PI / 180.0 * (lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(kotlin.math.PI / 180.0 * lat1) *
        cos(kotlin.math.PI / 180.0 * lat2) *
        sin(dLng / 2) * sin(dLng / 2)
    return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun formatDistanceLabel(distanceMeters: Double): String {
    if (distanceMeters < 1000) return "${distanceMeters.toInt()} m"
    val km = distanceMeters / 1000.0
    val oneDecimal = ((km * 10).toInt() / 10.0).toString()
    return if (oneDecimal.endsWith(".0")) "${km.toInt()} km" else "$oneDecimal km"
}
