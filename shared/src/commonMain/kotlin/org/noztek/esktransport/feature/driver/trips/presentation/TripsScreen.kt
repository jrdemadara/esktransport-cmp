package org.noztek.esktransport.feature.driver.trips.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.CurrencyDollar
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.Wallet
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.formatApiDateForDisplay
import org.noztek.esktransport.core.utils.formatApiTimeForDisplay
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTrip
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripStatus
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripsSummary
import kotlin.math.roundToInt

@Composable
fun TripsScreen(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: TripsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboard = uiState.dashboard

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TripsTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.TRIPS,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            dashboard?.let { data ->
                item {
                    TripsSummaryCard(
                        summary = data.summary,
                        currency = data.currency,
                    )
                }
                item {
                    TripFilterRow(selected = "All")
                }
                if (data.trips.isEmpty() && !uiState.isLoading) {
                    item {
                        TripsEmptyState()
                    }
                } else {
                    items(data.trips, key = { it.bookingPublicId }) { trip ->
                        TripHistoryCard(trip = trip)
                    }
                }
            }
            if (uiState.isLoading || (dashboard == null && uiState.errorMessage == null)) {
                item {
                    TripsLoadingState()
                }
            }
            uiState.errorMessage?.let { message ->
                item {
                    TripsErrorState(message = message)
                }
            }
        }
    }
}

@Composable
private fun TripsTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                )
            }
        },
        title = {
            Text(
                text = "Trips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.MagnifyingGlass,
                    contentDescription = "Search trips",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.AdjustmentsHorizontal,
                    contentDescription = "Filter trips",
                )
            }
        },
    )
}

@Composable
private fun TripsSummaryCard(
    summary: DriverTripsSummary,
    currency: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SoftIcon(
                        icon = Heroicons.Outline.CalendarDays,
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        iconColor = MaterialTheme.colorScheme.primary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = summary.from.formatApiDateForDisplay(fallback = "Today"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SummaryMetric(
                    icon = Heroicons.Outline.CheckCircle,
                    value = summary.completedTrips.toString(),
                    label = "Completed",
                    iconColor = Color(0xFF13A85B),
                    modifier = Modifier.weight(1f),
                )
                SummaryDivider()
                SummaryMetric(
                    icon = Heroicons.Outline.Clock,
                    value = summary.onlineSeconds.formatOnlineDuration(),
                    label = "Online",
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                SummaryDivider()
                SummaryMetric(
                    icon = Heroicons.Outline.CurrencyDollar,
                    value = formatTripAmount(summary.grossFare, currency),
                    label = "Gross",
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                )
                SummaryDivider()
                SummaryMetric(
                    icon = Heroicons.Outline.Wallet,
                    value = formatTripAmount(summary.netEarning, currency),
                    label = "Net",
                    iconColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        SoftIcon(
            icon = icon,
            backgroundColor = iconColor.copy(alpha = 0.12f),
            iconColor = iconColor,
            size = 34.dp,
            iconSize = 18.dp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .size(height = 52.dp, width = 1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun TripFilterRow(selected: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("All", "Completed", "Cancelled", "Ongoing").forEach { label ->
            FilterPill(
                label = label,
                selected = label == selected,
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = content,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(999.dp),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TripHistoryCard(trip: DriverTrip) {
    val statusColors = trip.status.colors()
    val stripeColor = statusColors.content

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize()
                    .width(3.dp)
                    .background(stripeColor),
            )
            Column(
                modifier = Modifier.padding(start = 14.dp, top = 13.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatusPill(status = trip.status)
                        LabeledValue(label = "Booking ID", value = trip.bookingPublicId.shortBookingId())
                    }

                    Row(
                        modifier = Modifier.weight(1.6f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        SoftIcon(
                            icon = Heroicons.Outline.User,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            iconColor = MaterialTheme.colorScheme.primary,
                            size = 36.dp,
                            iconSize = 19.dp,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = trip.passengerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = trip.vehicleTypeCode.vehicleTypeLabel(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            RouteTimeline(
                                pickup = trip.pickup.label.orPlaceholder("Pickup"),
                                dropoff = trip.dropoff.label.orPlaceholder("Dropoff"),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(0.8f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = trip.fareLabel(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Heroicons.Outline.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        trip.paymentMethod?.let { paymentMethod ->
                            SmallChip(text = paymentMethod.paymentMethodLabel())
                        }
                        TripMeasureRow(icon = Heroicons.Outline.MapPin, value = trip.distanceLabel())
                        TripMeasureRow(icon = Heroicons.Outline.Clock, value = trip.durationLabel())
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    CompactTripTime(
                        label = "Requested",
                        value = trip.requestedAt.formatApiTimeForDisplay(),
                        modifier = Modifier.weight(1f),
                    )
                    CompactTripTime(
                        label = "Accepted",
                        value = trip.acceptedAt.formatApiTimeForDisplay(),
                        modifier = Modifier.weight(1f),
                    )
                    CompactTripTime(
                        label = "Completed",
                        value = (trip.completedAt ?: trip.canceledAt).formatApiTimeForDisplay(),
                        modifier = Modifier.weight(1f),
                    )
                    CompactTripTime(
                        label = trip.bottomEndLabel(),
                        value = trip.bottomEndValue(),
                        valueColor = trip.bottomEndColor(),
                        alignEnd = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteTimeline(
    pickup: String,
    dropoff: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 3.dp),
        ) {
            RouteDot(Color(0xFF13A85B))
            Box(
                modifier = Modifier
                    .size(width = 1.dp, height = 18.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            RouteDot(Color(0xFFFF4D4F))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = pickup,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dropoff,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RouteDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun StatusPill(status: DriverTripStatus) {
    val colors = status.colors()
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = colors.container,
        contentColor = colors.content,
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SmallChip(text: String) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TripMeasureRow(
    icon: ImageVector,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CompactTripTime(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TripsLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun TripsEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoftIcon(
                icon = Heroicons.Outline.QueueList,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                iconColor = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "No trips yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Completed and active trips will show here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TripsErrorState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SoftIcon(
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = iconColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = iconColor,
            )
        }
    }
}

private fun DriverTripStatus.colors(): TripStatusColors {
    return when (this) {
        DriverTripStatus.Completed -> TripStatusColors(
            container = Color(0xFFE3F7EA),
            content = Color(0xFF13A85B),
        )
        DriverTripStatus.Cancelled -> TripStatusColors(
            container = Color(0xFFFFE8D6),
            content = Color(0xFFF97316),
        )
        DriverTripStatus.Expired -> TripStatusColors(
            container = Color(0xFFF1F5F9),
            content = Color(0xFF64748B),
        )
        DriverTripStatus.Offered,
        DriverTripStatus.Accepted,
        DriverTripStatus.ArrivingPickup,
        DriverTripStatus.InProgress,
        DriverTripStatus.Unknown -> TripStatusColors(
            container = Color(0xFFE8F1FF),
            content = Color(0xFF0B6BFF),
        )
    }
}

@Composable
private fun DriverTrip.bottomEndColor(): Color {
    return when (status) {
        DriverTripStatus.Completed -> Color(0xFF13A85B)
        DriverTripStatus.Cancelled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.primary
    }
}

private data class TripStatusColors(
    val container: Color,
    val content: Color,
)

private val DriverTripStatus.label: String
    get() = when (this) {
        DriverTripStatus.Offered -> "Offered"
        DriverTripStatus.Accepted -> "Accepted"
        DriverTripStatus.ArrivingPickup -> "Pickup"
        DriverTripStatus.InProgress -> "Ongoing"
        DriverTripStatus.Completed -> "Completed"
        DriverTripStatus.Cancelled -> "Cancelled"
        DriverTripStatus.Expired -> "Expired"
        DriverTripStatus.Unknown -> "Trip"
    }

private fun DriverTrip.fareLabel(): String {
    return finalFare?.let { formatTripAmount(it, currency) } ?: "-"
}

private fun DriverTrip.distanceLabel(): String {
    return distanceKm?.let { "${formatDecimal(it)} km" } ?: "-"
}

private fun DriverTrip.durationLabel(): String {
    return durationMin?.let { "$it min" } ?: "-"
}

private fun DriverTrip.bottomEndLabel(): String {
    return when (status) {
        DriverTripStatus.Completed -> "Net"
        DriverTripStatus.Cancelled,
        DriverTripStatus.Expired -> "Reason"
        else -> "Status"
    }
}

private fun DriverTrip.bottomEndValue(): String {
    return when (status) {
        DriverTripStatus.Completed -> settlement?.let { formatTripAmount(it.netEarning, currency) } ?: "-"
        DriverTripStatus.Cancelled,
        DriverTripStatus.Expired -> cancelReason?.reasonLabel() ?: status.label
        else -> status.label
    }
}

private fun String?.vehicleTypeLabel(): String {
    return when (this?.lowercase()) {
        "motorcycle" -> "Motorcycle"
        "tricycle" -> "Tricycle"
        "car" -> "Car"
        "sedan" -> "Sedan"
        "suv" -> "SUV"
        null, "" -> "Ride"
        else -> replaceFirstChar { char -> char.uppercase() }
    }
}

private fun String.paymentMethodLabel(): String {
    return when (lowercase()) {
        "cash" -> "Cash"
        else -> replaceFirstChar { char -> char.uppercase() }
    }
}

private fun String?.orPlaceholder(placeholder: String): String {
    return this?.takeIf { it.isNotBlank() } ?: placeholder
}

private fun String.shortBookingId(): String {
    return if (length > 18) take(18) else this
}

private fun String?.reasonLabel(): String? {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return value
        .removeSuffix(".")
        .replace("Cancelled by rider from mobile app", "Driver cancelled")
        .replace("Cancelled by passenger from mobile app", "Passenger cancelled")
}

private fun Long.formatOnlineDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

private fun formatTripAmount(amount: Double, currency: String): String {
    val cents = (amount * 100).roundToInt().coerceAtLeast(0)
    val whole = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    val prefix = when (currency.uppercase()) {
        "PHP" -> "₱"
        "USD" -> "\$"
        else -> "${currency.uppercase()} "
    }
    return "$prefix${formatWholeNumber(whole)}.$fraction"
}

private fun formatWholeNumber(value: Int): String {
    val raw = value.toString()
    return raw.reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

private fun formatDecimal(value: Double): String {
    val tenths = (value * 10).roundToInt()
    val whole = tenths / 10
    val fraction = tenths % 10
    return "$whole.$fraction"
}
