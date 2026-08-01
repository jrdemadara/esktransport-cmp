package org.noztek.esktransport.feature.passenger.activity.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowPath
import com.composables.icons.heroicons.outline.ArrowUpTray
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.RectangleStack
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.Wallet
import com.composables.icons.heroicons.outline.XCircle
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBooking
import org.noztek.esktransport.feature.common.active_booking.domain.model.ActiveBookingStatus
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletLedgerEntry
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerPendingBooking
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerPendingBookingStatus
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerRideActivity
import org.noztek.esktransport.feature.passenger.activity.domain.model.PassengerRideActivityStatus
import org.noztek.esktransport.feature.passenger.wallet.presentation.WalletViewModel

@Composable
fun ActivityScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onTrackTripClick: (String) -> Unit = {},
    activityViewModel: ActivityViewModel = koinViewModel(),
    walletViewModel: WalletViewModel = koinViewModel(),
) {
    val activityUiState by activityViewModel.uiState.collectAsState()
    val walletUiState by walletViewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(ActivityFilter.All) }
    val walletRows = walletUiState.recentLedgerEntries
        .take(3)
        .map { it.toWalletActivityRow() }
    val rideRows = activityUiState.recentRides
        .map { it.toRideActivityItem() }
        .filterFor(selectedFilter)
    val pendingRows = activityUiState.pendingBookings
        .map { it.toPendingBookingItem() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (activityUiState.isLoadingActiveBooking || activityUiState.activeBooking != null) {
            ActiveBookingCard(
                activeBooking = activityUiState.activeBooking,
                isLoading = activityUiState.isLoadingActiveBooking,
                onTrackTripClick = onTrackTripClick,
            )
        }
        ActivityFilterRow(
            selected = selectedFilter,
            onSelected = { selectedFilter = it },
        )
        if (selectedFilter == ActivityFilter.All || selectedFilter == ActivityFilter.Rides ||
            selectedFilter == ActivityFilter.Cancelled || selectedFilter == ActivityFilter.Completed
        ) {
            RecentRidesSection(
                rides = rideRows,
                isLoading = activityUiState.isLoadingActivity,
            )
        }
        if (selectedFilter == ActivityFilter.All || selectedFilter == ActivityFilter.Rides) {
            PendingBookingsSection(
                bookings = pendingRows,
                isLoading = activityUiState.isLoadingActivity,
            )
        }
        if (selectedFilter == ActivityFilter.All || selectedFilter == ActivityFilter.Wallet) {
            WalletActivitySection(rows = walletRows)
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ActiveBookingCard(
    activeBooking: ActiveBooking?,
    isLoading: Boolean,
    onTrackTripClick: (String) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val canTrack = activeBooking?.status.canTrackTrip()
    val destinationLabel = activeBooking?.destination?.label?.takeIf { it.isNotBlank() } ?: "Destination"
    val driverName = activeBooking?.driver?.name?.takeIf { it.isNotBlank() } ?: "Finding driver"
    val vehicleLabel = activeBooking?.driver?.vehicleLabel
        ?: activeBooking?.driver?.vehicleTypeCode?.replaceFirstChar { it.uppercase() }
        ?: "Ride"
    val fareLabel = activeBooking?.finalFare?.let {
        formatWalletAmount(it, activeBooking.currency ?: "PHP")
    } ?: "Fare pending"
    val detailLabel = if (isLoading) {
        "Loading booking details"
    } else {
        "$vehicleLabel • $driverName • $fareLabel"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        contentColor = Color.White,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primary,
                            primary.copy(alpha = 0.88f),
                        ),
                    ),
                )
                .padding(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color.White,
                    contentColor = primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.RectangleStack,
                            contentDescription = null,
                            modifier = Modifier.size(29.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = "Active booking",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                    Text(
                        text = if (isLoading) "Loading..." else "To $destinationLabel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = detailLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = activeBooking?.status.statusDescription(isLoading),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.84f),
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        contentColor = primary,
                    ) {
                        Text(
                            text = activeBooking?.status.statusLabel(isLoading),
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable(
                            enabled = canTrack,
                            onClick = {
                                activeBooking?.bookingPublicId?.let(onTrackTripClick)
                            },
                        ),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = if (canTrack) 1f else 0.72f),
                        contentColor = primary,
                    ) {
                        Text(
                            text = if (canTrack) "Track trip" else "Waiting",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFilterRow(
    selected: ActivityFilter,
    onSelected: (ActivityFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ActivityFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                modifier = Modifier.clickable { onSelected(filter) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = if (isSelected) {
                    null
                } else {
                    BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                },
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RecentRidesSection(
    rides: List<RideActivityItem>,
    isLoading: Boolean,
) {
    ActivitySection(title = "Recent rides") {
        if (isLoading) {
            EmptyActivityRow("Loading recent rides...")
        } else if (rides.isEmpty()) {
            EmptyActivityRow("No rides for this filter.")
        } else {
            rides.forEachIndexed { index, ride ->
                RideActivityRow(ride = ride)
                if (index < rides.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                }
            }
        }
    }
}

@Composable
private fun RideActivityRow(ride: RideActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
                ActivityIconBubble(icon = ride.icon, color = ride.iconColor)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "${ride.pickup} → ${ride.dropoff}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = ride.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusPill(status = ride.status)
                Text(
                    text = ride.timeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        Text(
            text = ride.fare,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PendingBookingsSection(
    bookings: List<PendingBookingItem>,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle("Pending bookings")
        ActivitySectionCard {
            when {
                isLoading -> EmptyActivityRow("Loading pending bookings...")
                bookings.isEmpty() -> EmptyActivityRow("No pending bookings.")
                else -> bookings.forEachIndexed { index, booking ->
                    PendingBookingRow(booking = booking)
                    if (index < bookings.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingBookingRow(booking: PendingBookingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivityIconBubble(
            icon = booking.icon,
            color = booking.iconColor,
            backgroundAlpha = 0.12f,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = booking.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = booking.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = booking.iconColor,
            )
            Text(
                text = booking.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = booking.fare,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WalletActivitySection(rows: List<WalletActivityRowItem>) {
    ActivitySection(title = "Wallet activity") {
        if (rows.isEmpty()) {
            EmptyActivityRow("No wallet activity yet.")
        } else {
            rows.forEachIndexed { index, row ->
                WalletActivityRow(row = row)
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                }
            }
        }
    }
}

@Composable
private fun WalletActivityRow(row: WalletActivityRowItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivityIconBubble(icon = row.icon, color = row.color)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = row.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (row.isCredit) CreditGreen else MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivitySection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle(title)
        ActivitySectionCard(content = content)
    }
}

@Composable
private fun ActivitySectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun ActivityIconBubble(
    icon: ImageVector,
    color: Color,
    backgroundAlpha: Float = 0.10f,
) {
    Surface(
        modifier = Modifier.size(45.dp),
        shape = CircleShape,
        color = color.copy(alpha = backgroundAlpha),
        contentColor = color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun StatusPill(status: RideActivityStatus) {
    val color = when (status) {
        RideActivityStatus.Completed -> CreditGreen
        RideActivityStatus.Cancelled -> Color(0xFFE53935)
    }
    val label = when (status) {
        RideActivityStatus.Completed -> "Completed"
        RideActivityStatus.Cancelled -> "Cancelled"
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyActivityRow(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(horizontal = 13.dp, vertical = 16.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private fun List<RideActivityItem>.filterFor(filter: ActivityFilter): List<RideActivityItem> {
    return when (filter) {
        ActivityFilter.All, ActivityFilter.Rides -> this
        ActivityFilter.Wallet -> emptyList()
        ActivityFilter.Cancelled -> filter { it.status == RideActivityStatus.Cancelled }
        ActivityFilter.Completed -> filter { it.status == RideActivityStatus.Completed }
    }
}

private fun PassengerRideActivity.toRideActivityItem(): RideActivityItem {
    val displayStatus = when (status) {
        PassengerRideActivityStatus.Completed -> RideActivityStatus.Completed
        PassengerRideActivityStatus.Cancelled,
        PassengerRideActivityStatus.Expired,
        PassengerRideActivityStatus.Unknown -> RideActivityStatus.Cancelled
    }
    val vehicleLabel = vehicleTypeCode?.toDisplayLabel() ?: bookingType.toDisplayLabel()
    val driverLabel = driverName?.takeIf { it.isNotBlank() } ?: "Driver"
    return RideActivityItem(
        pickup = pickup.label?.takeIf { it.isNotBlank() } ?: "Pickup",
        dropoff = dropoff.label?.takeIf { it.isNotBlank() } ?: "Destination",
        detail = "$vehicleLabel • $driverLabel",
        fare = finalFare?.let { formatWalletAmount(it, currency) } ?: "—",
        timeLabel = (activityAt ?: completedAt ?: canceledAt ?: requestedAt).formatApiDateTimeForDisplay(),
        status = displayStatus,
        icon = if (displayStatus == RideActivityStatus.Cancelled) Heroicons.Outline.XCircle else Heroicons.Outline.Truck,
        iconColor = if (displayStatus == RideActivityStatus.Cancelled) Color(0xFFE53935) else MaterialThemeBlue,
    )
}

private fun PassengerPendingBooking.toPendingBookingItem(): PendingBookingItem {
    val vehicleLabel = vehicleTypeCode?.toDisplayLabel() ?: bookingType.toDisplayLabel()
    val statusText = when (status) {
        PassengerPendingBookingStatus.Searching -> "Searching for driver"
        PassengerPendingBookingStatus.Offered -> "Waiting for driver response"
        PassengerPendingBookingStatus.Unknown -> "Pending"
    }
    val routeLabel = listOfNotNull(
        pickupLabel?.takeIf { it.isNotBlank() },
        dropoffLabel?.takeIf { it.isNotBlank() },
    ).joinToString(" → ").ifBlank { "Route pending" }
    return PendingBookingItem(
        title = "$vehicleLabel request",
        statusLabel = statusText,
        subtitle = "$routeLabel • ${requestedAt.formatApiDateTimeForDisplay()}",
        fare = finalFare?.let { formatWalletAmount(it, currency) } ?: "—",
        icon = Heroicons.Outline.Truck,
        iconColor = Color(0xFFF06423),
    )
}

private fun String.toDisplayLabel(): String {
    return replace("_", " ")
        .replace("-", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
        .ifBlank { "Ride" }
}

private fun ActiveBookingStatus?.canTrackTrip(): Boolean {
    return this == ActiveBookingStatus.ACCEPTED ||
        this == ActiveBookingStatus.ARRIVING_PICKUP ||
        this == ActiveBookingStatus.IN_PROGRESS
}

private fun ActiveBookingStatus?.statusLabel(isLoading: Boolean): String {
    if (isLoading) return "Loading"
    return when (this) {
        ActiveBookingStatus.SEARCHING -> "Searching"
        ActiveBookingStatus.OFFERED -> "Offered"
        ActiveBookingStatus.ACCEPTED -> "Driver assigned"
        ActiveBookingStatus.ARRIVING_PICKUP -> "Driver arriving"
        ActiveBookingStatus.IN_PROGRESS -> "On trip"
        ActiveBookingStatus.UNKNOWN, null -> "Active"
    }
}

private fun ActiveBookingStatus?.statusDescription(isLoading: Boolean): String {
    if (isLoading) return "Checking your current booking"
    return when (this) {
        ActiveBookingStatus.SEARCHING -> "Finding nearby drivers"
        ActiveBookingStatus.OFFERED -> "Waiting for a driver to respond"
        ActiveBookingStatus.ACCEPTED -> "Driver is preparing for pickup"
        ActiveBookingStatus.ARRIVING_PICKUP -> "Driver is on the way to pickup"
        ActiveBookingStatus.IN_PROGRESS -> "On the way to destination"
        ActiveBookingStatus.UNKNOWN, null -> "Booking is active"
    }
}

private fun WalletLedgerEntry.toWalletActivityRow(): WalletActivityRowItem {
    val isCredit = direction.equals("credit", ignoreCase = true)
    val icon = when (entryType) {
        "topup_credit" -> Heroicons.Outline.ArrowUpTray
        "cashout_debit" -> Heroicons.Outline.Wallet
        "refund_credit" -> Heroicons.Outline.ArrowPath
        "platform_fee_debit" -> Heroicons.Outline.Wallet
        else -> if (isCredit) Heroicons.Outline.ArrowUpTray else Heroicons.Outline.Wallet
    }
    return WalletActivityRowItem(
        title = description?.takeIf { it.isNotBlank() } ?: entryType.toWalletTitle(),
        subtitle = createdAt.formatApiDateTimeForDisplay(),
        amount = "${if (isCredit) "+" else "-"}${formatWalletAmount(amount, currency)}",
        icon = icon,
        color = if (isCredit) CreditGreen else MaterialThemeBlue,
        isCredit = isCredit,
    )
}

private fun String.toWalletTitle(): String {
    return when (this) {
        "topup_credit" -> "Wallet top-up"
        "cashout_debit" -> "Cashout"
        "platform_fee_debit" -> "Ride payment"
        "refund_credit" -> "Refund"
        else -> replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

private fun formatWalletAmount(amount: Double, currency: String): String {
    val prefix = if (currency.equals("PHP", ignoreCase = true)) "₱" else "$currency "
    val rounded = (amount * 100).toInt()
    val whole = rounded / 100
    val decimals = (rounded % 100).toString().padStart(2, '0')
    return "$prefix$whole.$decimals"
}

private enum class ActivityFilter(val label: String) {
    All("All"),
    Rides("Rides"),
    Wallet("Wallet"),
    Cancelled("Cancelled"),
    Completed("Completed"),
}

private enum class RideActivityStatus {
    Completed,
    Cancelled,
}

private data class RideActivityItem(
    val pickup: String,
    val dropoff: String,
    val detail: String,
    val fare: String,
    val timeLabel: String,
    val status: RideActivityStatus,
    val icon: ImageVector,
    val iconColor: Color,
)

private data class PendingBookingItem(
    val title: String,
    val statusLabel: String,
    val subtitle: String,
    val fare: String,
    val icon: ImageVector,
    val iconColor: Color,
)

private data class WalletActivityRowItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val icon: ImageVector,
    val color: Color,
    val isCredit: Boolean,
)

private val CreditGreen = Color(0xFF118447)
private val MaterialThemeBlue = Color(0xFF2563EB)
