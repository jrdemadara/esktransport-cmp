package org.noztek.esktransport.feature.passenger.wallet.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowDownTray
import com.composables.icons.heroicons.outline.ArrowPath
import com.composables.icons.heroicons.outline.ArrowUpTray
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.Wallet
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.feature.common.wallet.domain.model.WalletLedgerEntry

@Composable
fun WalletScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onTopUpClick: () -> Unit = {},
    onCashoutClick: () -> Unit = {},
    viewModel: WalletViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pendingTopupAmount = uiState.pendingTopups
        .filter { it.status == "pending" }
        .sumOf { it.amount }
    val pendingCashoutAmount = uiState.pendingCashouts
        .filter { it.status == "pending" }
        .sumOf { it.amount }
    val transactions = uiState.recentLedgerEntries.map { it.toWalletTransactionItem() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WalletBalanceCard(
            balanceLabel = formatWalletAmount(uiState.balance, uiState.currency),
            isLoading = uiState.isLoading,
            onTopUpClick = onTopUpClick,
            onCashoutClick = onCashoutClick,
        )
        WalletSummaryCard(
            pendingTopupLabel = formatWalletAmount(pendingTopupAmount, uiState.currency),
            pendingCashoutLabel = formatWalletAmount(pendingCashoutAmount, uiState.currency),
        )
        uiState.errorMessage?.let { message ->
            WalletNoticeCard(message = message)
        }
        RecentTransactionsSection(transactions = transactions)
        PaymentsProtectedCard()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun WalletBalanceCard(
    balanceLabel: String,
    isLoading: Boolean,
    onTopUpClick: () -> Unit,
    onCashoutClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF075BE8),
        contentColor = Color.White,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F6BF2).copy(alpha = 0.16f),
                            Color(0xFF0649C7).copy(alpha = 0.70f),
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Available balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                        Text(
                            text = if (isLoading) "Loading..." else balanceLabel,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            text = "Usable for rides, rentals, and cargo bookings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                            color = Color.White.copy(alpha = 0.88f),
                        )
                    } else {
                        Icon(
                            imageVector = Heroicons.Outline.Wallet,
                            contentDescription = null,
                            modifier = Modifier.size(58.dp),
                            tint = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
                WalletActionTray(
                    onTopUpClick = onTopUpClick,
                    onCashoutClick = onCashoutClick,
                )
            }
        }
    }
}

@Composable
private fun WalletActionTray(
    onTopUpClick: () -> Unit,
    onCashoutClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WalletTrayAction(
                label = "Top up",
                icon = Heroicons.Outline.Plus,
                onClick = onTopUpClick,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(height = 30.dp, alpha = 0.38f)
            WalletTrayAction(
                label = "Send",
                icon = Heroicons.Outline.PaperAirplane,
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(height = 30.dp, alpha = 0.38f)
            WalletTrayAction(
                label = "Withdraw",
                icon = Heroicons.Outline.ArrowDownTray,
                onClick = onCashoutClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WalletTrayAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WalletSummaryCard(
    pendingTopupLabel: String,
    pendingCashoutLabel: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WalletSummaryCell(
                title = "Pending top-up",
                value = pendingTopupLabel,
                icon = Heroicons.Outline.ArrowPath,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(height = 42.dp)
            WalletSummaryCell(
                title = "Pending cashout",
                value = pendingCashoutLabel,
                icon = Heroicons.Outline.Wallet,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WalletSummaryCell(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RecentTransactionsSection(transactions: List<WalletTransactionItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionTitle("Recent transactions")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
        ) {
            Column {
                if (transactions.isEmpty()) {
                    Text(
                        text = "No wallet activity yet.",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    transactions.forEachIndexed { index, transaction ->
                        WalletTransactionRow(transaction = transaction)
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 66.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletTransactionRow(transaction: WalletTransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = transaction.color.copy(alpha = 0.12f),
            contentColor = transaction.color,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = transaction.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = transaction.amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isCredit) CreditGreen else MaterialTheme.colorScheme.onSurface,
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
private fun WalletNoticeCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f),
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PaymentsProtectedCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Outline.ShieldCheck,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Payments protected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Your money is safe and secure with us.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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

@Composable
private fun VerticalDivider(
    height: androidx.compose.ui.unit.Dp = 48.dp,
    alpha: Float = 0.58f,
) {
    Spacer(
        modifier = Modifier
            .size(width = 1.dp, height = height)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)),
    )
}

private data class WalletTransactionItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val icon: ImageVector,
    val color: Color,
    val isCredit: Boolean,
)

private val CreditGreen = Color(0xFF118447)
private val DebitRed = Color(0xFFE53935)
private val AccentBlue = Color(0xFF2563EB)

private fun WalletLedgerEntry.toWalletTransactionItem(): WalletTransactionItem {
    val isCredit = direction.equals("credit", ignoreCase = true)
    val icon = when (entryType) {
        "topup_credit" -> Heroicons.Outline.ArrowUpTray
        "cashout_debit" -> Heroicons.Outline.ArrowDownTray
        "refund_credit" -> Heroicons.Outline.ArrowPath
        "platform_fee_debit" -> Heroicons.Outline.Wallet
        else -> if (isCredit) Heroicons.Outline.ArrowUpTray else Heroicons.Outline.Wallet
    }
    val color = when {
        isCredit -> CreditGreen
        entryType == "cashout_debit" -> DebitRed
        else -> AccentBlue
    }
    return WalletTransactionItem(
        title = description?.takeIf { it.isNotBlank() } ?: entryType.toWalletTitle(),
        subtitle = createdAt.formatApiDateTimeForDisplay(),
        amount = "${if (isCredit) "+" else "-"}${formatWalletAmount(amount, currency)}",
        icon = icon,
        color = color,
        isCredit = isCredit,
    )
}

private fun String.toWalletTitle(): String {
    return when (this) {
        "topup_credit" -> "Wallet top-up"
        "cashout_debit" -> "Cashout"
        "platform_fee_debit" -> "Platform fee"
        "adjustment_credit", "adjustment_debit" -> "Wallet adjustment"
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
