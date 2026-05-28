package org.noztek.esktransport.feature.passenger.booking_review.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Users
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
private fun SearchingSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Searching for driver", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Your booking request has been sent. Please wait while we find a driver.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
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
                    Icon(Lucide.Users, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$seatCount seats")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Map, contentDescription = null, modifier = Modifier.size(16.dp))
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
        Icon(Lucide.MapPin, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconColor)
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
