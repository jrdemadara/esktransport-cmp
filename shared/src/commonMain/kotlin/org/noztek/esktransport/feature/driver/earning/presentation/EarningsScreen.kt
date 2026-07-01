package org.noztek.esktransport.feature.driver.earning.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.home_car
import esktransport.shared.generated.resources.home_scooter
import esktransport.shared.generated.resources.home_tricycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.ui.composables.driver.DriverTopBar

enum class DriverEarningsVehicleType {
    Motorcycle,
    Tricycle,
    Car,
}

@Composable
fun EarningsScreen(
    todaysEarning: String = "PHP 0.00",
    totalTrips: Int = 0,
    vehicleType: DriverEarningsVehicleType = DriverEarningsVehicleType.Motorcycle,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            DriverTopBar(
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
            )
        },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.EARNINGS,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EarningsSummaryCard(
                todaysEarning = todaysEarning,
                totalTrips = totalTrips,
                vehicleType = vehicleType,
            )
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    todaysEarning: String,
    totalTrips: Int,
    vehicleType: DriverEarningsVehicleType,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(142.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Today's Earning",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = todaysEarning,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$totalTrips completed trips",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            Box(
                modifier = Modifier.size(width = 124.dp, height = 98.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(vehicleType.illustration),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

private val DriverEarningsVehicleType.illustration: DrawableResource
    get() = when (this) {
        DriverEarningsVehicleType.Motorcycle -> Res.drawable.home_scooter
        DriverEarningsVehicleType.Tricycle -> Res.drawable.home_tricycle
        DriverEarningsVehicleType.Car -> Res.drawable.home_car
    }
