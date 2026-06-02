package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Bolt
import com.composables.icons.heroicons.outline.Map
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Users
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
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
    val stateInput = uiState.input ?: input
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
            initialValue = SheetValue.Expanded,
            skipHiddenState = true,
        ),
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = Color.Transparent,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            if (uiState.isSearchingForRider) {
                SearchingSheet()
            } else {
                ReviewSheet(
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
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    // Infinite transitions for subtle, premium micro-animations
    val infiniteTransition = rememberInfiniteTransition(label = "SearchingAnimations")

    // Shimmer offset progress for the custom progress bar
    val progressTranslation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )
    // Pulse alpha for the LIVE status green dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveIndicatorPulse"
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
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // A small rounded icon container, around 34.dp
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape) // Rounded iOS-style circular accent container
                        .background(primaryColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Bolt icon custom drawn for high-fidelity Apple SF-Symbol look (around 18.dp)
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.56f, size.height * 0.04f)
                            lineTo(size.width * 0.20f, size.height * 0.54f)
                            lineTo(size.width * 0.50f, size.height * 0.54f)
                            lineTo(size.width * 0.44f, size.height * 0.96f)
                            lineTo(size.width * 0.80f, size.height * 0.46f)
                            lineTo(size.width * 0.50f, size.height * 0.46f)
                            close()
                        }
                        drawPath(path, color = primaryColor)
                    }
                }
                // Title & Subtitle block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Main title: "Finding your driver" (titleMedium, semi-bold weight)
                    Text(
                        text = "Finding your driver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor,
                        letterSpacing = (-0.3).sp // Apple-style tight tracking
                    )
                    // Subtitle: "Request sent. Matching nearby online drivers." (bodySmall, secondary color)
                    Text(
                        text = "Request sent. Matching nearby online drivers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor,
                        lineHeight = 14.sp
                    )
                }
                // A small "LIVE" status label on the trailing side
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Pulse dot to give it a reactive, active native look
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .graphicsLayer(alpha = pulseAlpha)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = primaryColor
                    )
                }
            }
            // A custom thin progress indicator, around 3.dp tall
            // Animates a smooth, organic iOS-like shimmer gradient across the screen width
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
        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Review Booking", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
        TripPointRow("Pickup", pickupLocation, MaterialTheme.colorScheme.primary)
        TripPointRow("Destination", destinationLocation, Color(0xFFEF4444))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(vehicleLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Standard Ride", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Heroicons.Outline.Users, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$seatCount seats")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Heroicons.Outline.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(distanceLabel)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth(), enabled = !isCreatingBooking) {
            Text(if (isCreatingBooking) "Confirming..." else "Confirm Booking")
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TripPointRow(label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Heroicons.Outline.MapPin, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
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
