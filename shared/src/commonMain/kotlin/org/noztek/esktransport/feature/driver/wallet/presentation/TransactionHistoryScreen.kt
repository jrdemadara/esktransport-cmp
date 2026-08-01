package org.noztek.esktransport.feature.driver.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowDownTray
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowPath
import com.composables.icons.heroicons.outline.ArrowUpTray
import com.composables.icons.heroicons.outline.Wallet
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletLedgerEntry
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: TransactionHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unable to load transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            uiState.entries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No transactions yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 18.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = "Recent wallet activity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    itemsIndexed(uiState.entries) { index, entry ->
                        TransactionRow(entry = entry)
                        if (index < uiState.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 66.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(entry: DriverWalletLedgerEntry) {
    val isCredit = entry.direction.equals("credit", ignoreCase = true)
    val accentColor = if (isCredit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.11f),
            contentColor = accentColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = entry.transactionIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = entry.title(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.createdAt.formatApiDateTimeForDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Balance after: ${formatWalletAmount(entry.balanceAfter, entry.currency)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = "${if (isCredit) "+" else "-"}${formatWalletAmount(abs(entry.amount), entry.currency)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isCredit) accentColor else MaterialTheme.colorScheme.error,
        )
    }
}

private fun DriverWalletLedgerEntry.transactionIcon() = when (entryType) {
    "topup_credit" -> Heroicons.Outline.ArrowUpTray
    "cashout_debit" -> Heroicons.Outline.ArrowDownTray
    "refund_credit" -> Heroicons.Outline.ArrowPath
    else -> Heroicons.Outline.Wallet
}

private fun DriverWalletLedgerEntry.title(): String {
    return description?.takeIf { it.isNotBlank() } ?: entryType
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }
}

private fun formatWalletAmount(amount: Double, currency: String): String {
    val prefix = if (currency.equals("PHP", ignoreCase = true)) "₱" else "$currency "
    val cents = (amount * 100).toInt()
    val whole = cents / 100
    val decimals = abs(cents % 100).toString().padStart(2, '0')
    return "$prefix$whole.$decimals"
}
