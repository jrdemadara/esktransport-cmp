package org.noztek.esktransport.feature.passenger.ride_planner.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Locate
import com.composables.icons.lucide.LocateFixed
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.MapPin
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RidePlannerScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onPickupClick: () -> Unit = {},
    onDestinationClick: () -> Unit = {},
    onUseCurrentLocationClick: () -> Unit = {},
    pickupLocation: String = "",
    destinationLocation: String = "",
    viewModel: RidePlannerViewModel = koinViewModel(),
) {
    val passengerCount by viewModel.passengerCount.collectAsState()
    val selectedVehicleType by viewModel.selectedVehicleType.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        item {
            PickupDestinationPanel(
                onPickupClick = onPickupClick,
                onDestinationClick = onDestinationClick,
                onUseCurrentLocationClick = onUseCurrentLocationClick,
                pickupLocation = pickupLocation,
                destinationLocation = destinationLocation,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Passengers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("1", "2", "3", "4", "5").forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 5),
                        onClick = { viewModel.setPassengerCount(index) },
                        selected = passengerCount == index,
                        label = { Text(label) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Vehicle Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("Moto", "Trike", "Car", "Van").forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                        onClick = { viewModel.setVehicleType(index) },
                        selected = selectedVehicleType == index,
                        label = { Text(label) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.onReviewBookingClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = pickupLocation.isNotBlank() && destinationLocation.isNotBlank(),
            ) { Text("Review Booking") }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PickupDestinationPanel(
    onPickupClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onUseCurrentLocationClick: () -> Unit,
    pickupLocation: String,
    destinationLocation: String,
) {
    val dottedLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Surface(color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .drawBehind {
                        val x = size.width / 2f
                        var y = 46f
                        val endY = size.height - 46f
                        while (y < endY) {
                            drawCircle(color = dottedLineColor, radius = 2f, center = Offset(x, y))
                            y += 10f
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LocationIcon()
                LocationIcon()
            }

            Column(modifier = Modifier.weight(1f)) {
                LocationTextBlock(
                    label = "Pickup Point",
                    value = pickupLocation.ifBlank { "My Current Location" },
                    onClick = if (pickupLocation.isBlank()) onUseCurrentLocationClick else onPickupClick,
                    actionIcon = pickupLocation.isBlank(),
                    onActionIconClick = onPickupClick,
                )
                Spacer(modifier = Modifier.height(28.dp))
                LocationTextBlock(
                    label = "Destination",
                    value = destinationLocation.ifBlank { "Where to?" },
                    onClick = onDestinationClick,
                    actionIcon = false,
                    onActionIconClick = onDestinationClick,
                )
            }
        }
    }
}

@Composable
private fun LocationIcon() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(Lucide.MapPin, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun LocationTextBlock(
    label: String,
    value: String,
    onClick: () -> Unit,
    actionIcon: Boolean,
    onActionIconClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Text(
                value,
                color = if (value == "My Current Location" || value == "Where to?") {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onClick),
            )
        }
        if (actionIcon) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Lucide.Map,
                contentDescription = "Search pickup location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onActionIconClick),
            )
        }
    }
}
