package org.noztek.esktransport.feature.driver.trip_tracking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DriverTripTrackingScreen(
    bookingId: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Driver Trip Tracking",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Booking ID: $bookingId",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Driver trip session screen wiring in progress.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

