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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.ui.composables.driver.DriverTopBar
import kotlin.math.roundToInt

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
    viewModel: EarningsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboard = uiState.dashboard
    val effectiveTodaysEarning = dashboard?.let { formatEarningAmount(it.today.netEarning, it.currency) } ?: todaysEarning
    val effectiveTotalTrips = dashboard?.today?.completedTrips ?: totalTrips

    LaunchedEffect(Unit) {
        viewModel.load()
    }

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
                todaysEarning = effectiveTodaysEarning,
                totalTrips = effectiveTotalTrips,
                vehicleType = vehicleType,
            )
            dashboard?.let {
                EarningsBreakdownPanel(
                    grossFare = formatEarningAmount(it.today.grossFare, it.currency),
                    platformFee = formatEarningAmount(it.today.platformFee, it.currency),
                    netEarning = formatEarningAmount(it.today.netEarning, it.currency),
                )
            }
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
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

@Composable
private fun EarningsBreakdownPanel(
    grossFare: String,
    platformFee: String,
    netEarning: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            EarningsMetricRow(label = "Cash collected", value = grossFare)
            EarningsMetricRow(label = "Platform fee", value = platformFee)
            HorizontalDividerCompat()
            EarningsMetricRow(label = "Net income", value = netEarning, strong = true)
        }
    }
}

@Composable
private fun EarningsMetricRow(
    label: String,
    value: String,
    strong: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (strong) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HorizontalDividerCompat() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    ) {}
}

private fun formatEarningAmount(amount: Double, currency: String): String {
    val cents = (amount * 100).roundToInt().coerceAtLeast(0)
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    val prefix = when (currency.uppercase()) {
        "PHP" -> "₱"
        "USD" -> "\$"
        else -> "${currency.uppercase()} "
    }
    return "$prefix$whole.$fraction"
}

private val DriverEarningsVehicleType.illustration: DrawableResource
    get() = when (this) {
        DriverEarningsVehicleType.Motorcycle -> Res.drawable.home_scooter
        DriverEarningsVehicleType.Tricycle -> Res.drawable.home_tricycle
        DriverEarningsVehicleType.Car -> Res.drawable.home_car
    }
