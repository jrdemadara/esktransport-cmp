package org.noztek.esktransport.feature.driver.earning.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowDown
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowUp
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.CurrencyDollar
import com.composables.icons.heroicons.outline.QuestionMarkCircle
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.outline.Wallet
import com.composables.icons.heroicons.outline.XCircle
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsDashboard
import org.noztek.esktransport.feature.driver.earning.domain.model.RiderEarningsSettlement
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletLedgerEntry
import kotlin.math.roundToInt

@Composable
fun EarningsScreen(
    onBackClick: () -> Unit,
    onTopUpClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: EarningsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            EarningsTopBar(onBackClick = onBackClick)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val dashboard = uiState.dashboard
            val walletDashboard = uiState.walletDashboard

            walletDashboard?.let { wallet ->
                item {
                    WalletBalanceCard(
                        dashboard = wallet,
                        onClick = onTopUpClick,
                    )
                }
            }

            dashboard?.let { earnings ->
                item {
                    TodayEarningsCard(dashboard = earnings)
                }
                item {
                    RecentSettlementsCard(settlements = earnings.recentSettlements)
                }
            }

            walletDashboard?.let { wallet ->
                item {
                    RecentWalletActivityCard(
                        entries = wallet.recentLedgerEntries,
                        onTopUpClick = onTopUpClick,
                    )
                }
            }

            if (uiState.isLoading || (dashboard == null && walletDashboard == null && uiState.error == null)) {
                item {
                    LoadingState()
                }
            }

            uiState.error?.let { message ->
                item {
                    ErrorState(message = message)
                }
            }
        }
    }
}

@Composable
private fun EarningsTopBar(onBackClick: () -> Unit) {
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
                text = "Earnings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.QuestionMarkCircle,
                    contentDescription = "Help",
                )
            }
        },
    )
}

@Composable
private fun WalletBalanceCard(
    dashboard: DriverWalletDashboard,
    onClick: () -> Unit,
) {
    val wallet = dashboard.wallet
    val requirement = dashboard.driverModeRequirement
    val meetsMinimum = requirement.hasMinimumWalletBalance
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color(0xFF075BE8),
        ),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        contentColor = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(18.dp))
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = "Wallet Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    Text(
                        text = formatAmount(wallet.balance, wallet.currency),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (meetsMinimum) Heroicons.Outline.CheckCircle else Heroicons.Outline.XCircle,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                            tint = if (meetsMinimum) Color(0xFF40E082) else Color(0xFFFFD166),
                        )
                        Text(
                            text = if (meetsMinimum) "Meets minimum balance" else "Below minimum balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    }
                    Text(
                        text = "Min. required: ${formatAmount(requirement.minimumWalletBalance, requirement.currency)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .height(72.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.22f)),
                    )
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Heroicons.Outline.Wallet,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = "Top up wallet",
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayEarningsCard(dashboard: RiderEarningsDashboard) {
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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Current service day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SoftIcon(
                    icon = Heroicons.Outline.CalendarDays,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    iconColor = MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TodayMetric(
                    icon = Heroicons.Outline.QueueList,
                    value = dashboard.today.completedTrips.toString(),
                    label = "Trips",
                    iconColor = Color(0xFF13A85B),
                    modifier = Modifier.weight(1f),
                )
                MetricDivider()
                TodayMetric(
                    icon = Heroicons.Outline.CurrencyDollar,
                    value = formatAmount(dashboard.today.grossFare, dashboard.currency),
                    label = "Gross",
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                MetricDivider()
                TodayMetric(
                    icon = Heroicons.Outline.CurrencyDollar,
                    value = formatAmount(dashboard.today.platformFee, dashboard.currency),
                    label = "Fee",
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                )
                MetricDivider()
                TodayMetric(
                    icon = Heroicons.Outline.Wallet,
                    value = formatAmount(dashboard.today.netEarning, dashboard.currency),
                    label = "Net",
                    iconColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TodayMetric(
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
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
private fun RecentSettlementsCard(settlements: List<RiderEarningsSettlement>) {
    SectionCard(
        title = "Recent Settlements",
        action = "View All",
    ) {
        if (settlements.isEmpty()) {
            EmptyLine("No settlements yet.")
        } else {
            Column {
                settlements.take(5).forEachIndexed { index, settlement ->
                    SettlementRow(settlement = settlement)
                    if (index < settlements.take(5).lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementRow(settlement: RiderEarningsSettlement) {
    Column(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftIcon(
                icon = Heroicons.Outline.CheckCircle,
                backgroundColor = Color(0xFF13A85B).copy(alpha = 0.10f),
                iconColor = Color(0xFF13A85B),
                size = 34.dp,
                iconSize = 18.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = settlement.publicId.shortId("SET"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = settlement.settledAt.dateTimeLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatAmount(settlement.netEarning, settlement.currency),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF13A85B),
                maxLines = 1,
            )
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            MiniAmount(label = "Gross", value = formatAmount(settlement.grossFare, settlement.currency), modifier = Modifier.weight(1f))
            MiniAmount(label = "Fee", value = formatAmount(settlement.platformFee, settlement.currency), modifier = Modifier.weight(1f))
            MiniAmount(label = "Net", value = formatAmount(settlement.netEarning, settlement.currency), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecentWalletActivityCard(
    entries: List<DriverWalletLedgerEntry>,
    onTopUpClick: () -> Unit,
) {
    SectionCard(
        title = "Recent Wallet Activity",
        action = "View All",
    ) {
        if (entries.isEmpty()) {
            EmptyLine("No wallet activity yet.")
        } else {
            Column {
                entries.take(4).forEachIndexed { index, entry ->
                    WalletActivityRow(entry = entry)
                    if (index < entries.take(4).lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                    }
                }
            }
        }
        TopUpPrompt(onTopUpClick = onTopUpClick)
    }
}

@Composable
private fun WalletActivityRow(entry: DriverWalletLedgerEntry) {
    val isCredit = entry.direction.equals("credit", ignoreCase = true)
    val color = if (isCredit) Color(0xFF13A85B) else Color(0xFFE53935)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftIcon(
            icon = if (isCredit) Heroicons.Outline.ArrowDown else Heroicons.Outline.ArrowUp,
            backgroundColor = color.copy(alpha = 0.12f),
            iconColor = color,
            size = 38.dp,
            iconSize = 19.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.entryType.entryTypeLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                DirectionPill(text = if (isCredit) "Credit" else "Debit", color = color)
            }
            Text(
                text = entry.description ?: "Balance after: ${formatAmount(entry.balanceAfter, entry.currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Balance after: ${formatAmount(entry.balanceAfter, entry.currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "${if (isCredit) "+" else "-"}${formatAmount(entry.amount, entry.currency)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
            )
            Text(
                text = entry.createdAt.dateTimeLabel(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun TopUpPrompt(onTopUpClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftIcon(
                icon = Heroicons.Outline.Wallet,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                iconColor = MaterialTheme.colorScheme.primary,
                size = 34.dp,
                iconSize = 18.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Top-up your wallet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Show a QR at the kiosk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onTopUpClick) {
                Text("Generate")
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    action: String,
    content: @Composable ColumnScope.() -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            content()
        }
    }
}

@Composable
private fun MiniAmount(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DirectionPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoadingState() {
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
private fun ErrorState(message: String) {
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

@Composable
private fun MetricDivider(height: androidx.compose.ui.unit.Dp = 52.dp) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .height(height)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

private fun formatAmount(amount: Double, currency: String): String {
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
    return value.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}

private fun String.shortId(prefix: String): String {
    val compact = replace("-", "").takeLast(8).uppercase()
    return "$prefix-$compact"
}

private fun String.entryTypeLabel(): String {
    return split("_", "-")
        .filter { it.isNotBlank() }
        .joinToString(" ") { value ->
            value.replaceFirstChar { char -> char.uppercase() }
        }
}

private fun String?.dateTimeLabel(): String {
    val value = this ?: return "-"
    val datePart = value.substringBefore('T').substringBefore(' ')
    val timePart = when {
        value.contains('T') -> value.substringAfter('T')
        value.contains(' ') -> value.substringAfter(' ')
        else -> ""
    }.take(5)
    return listOfNotNull(
        datePart.takeIf { it.isNotBlank() },
        timePart.takeIf { it.isNotBlank() },
    ).joinToString(" • ").ifBlank { "-" }
}
