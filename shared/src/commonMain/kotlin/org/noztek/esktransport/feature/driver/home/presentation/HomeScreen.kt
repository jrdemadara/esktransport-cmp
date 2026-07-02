package org.noztek.esktransport.feature.driver.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowDown
import com.composables.icons.heroicons.outline.ArrowUp
import com.composables.icons.heroicons.outline.ArrowUpRight
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.solid.Star
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.ui.composables.driver.DriverTopBar
import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleInfo
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletTopup
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    statsRefreshToken: Long = 0L,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    onSetupClick: (DriverOnboardingStatus?) -> Unit = {},
    onDriverModeClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOnboardingStatus(showLoading = false)
                viewModel.refreshStats(showLoading = false)
                viewModel.refreshWallet(showLoading = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(statsRefreshToken) {
        if (statsRefreshToken > 0L) {
            viewModel.refreshStats(showLoading = false)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DriverTopBar(
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
            )
        },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.HOME,
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
            DriverWalletCard(
                isLoading = uiState.isLoadingWallet,
                isCreatingTopup = uiState.isCreatingTopup,
                dashboard = uiState.walletDashboard,
                selectedTopup = uiState.selectedTopup,
                errorMessage = uiState.walletErrorMessage,
                onCreateTopup = viewModel::createTopup,
                onClearSelectedTopup = viewModel::clearSelectedTopup,
                onRetryClick = viewModel::refreshWallet,
            )
            TotalStatsPanel(
                isLoading = uiState.isLoadingStats,
                stats = uiState.stats,
                errorMessage = uiState.statsErrorMessage,
                onRetryClick = viewModel::refreshStats,
            )
            if (uiState.onboardingStatus?.canGo == true) {
                val walletRequirement = uiState.walletDashboard?.driverModeRequirement
                val hasRequiredBalance = walletRequirement?.hasMinimumWalletBalance == true
                AppPrimaryButton(
                    text = "Switch to Driver Mode",
                    onClick = onDriverModeClick,
                    enabled = hasRequiredBalance,
                    height = 44.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                    },
                )
                if (walletRequirement != null && !hasRequiredBalance) {
                    Text(
                        text = "Add at least ${formatWalletAmount(walletRequirement.minimumWalletBalance, walletRequirement.currency)} to start Driver Mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DriverSetupCard(
                isLoading = uiState.isLoadingSetup,
                status = uiState.onboardingStatus,
                errorMessage = uiState.errorMessage,
                onSetupClick = { onSetupClick(uiState.onboardingStatus) },
                onRetryClick = viewModel::refreshOnboardingStatus,
            )
        }
    }
}

@Composable
private fun DriverWalletCard(
    isLoading: Boolean,
    isCreatingTopup: Boolean,
    dashboard: DriverWalletDashboard?,
    selectedTopup: DriverWalletTopup?,
    errorMessage: String?,
    onCreateTopup: (Double) -> Unit,
    onClearSelectedTopup: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val balanceLabel = dashboard?.wallet?.let { formatWalletAmount(it.balance, it.currency) } ?: "PHP 0.00"
    val pendingTopup = selectedTopup ?: dashboard?.pendingTopups?.firstOrNull()
    val requirement = dashboard?.driverModeRequirement
    val isBusy = isLoading || isCreatingTopup
    val brandBlue = MaterialTheme.colorScheme.primaryContainer
    val walletGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4F8DFF),
            brandBlue,
            Color(0xFF002D88),
        ),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(walletGradient),
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp)
                        .size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Total balance",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = balanceLabel,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    WalletQuickAction(
                        label = "Top Up",
                        icon = Heroicons.Outline.ArrowUp,
                        enabled = !isBusy,
                        onClick = { onCreateTopup(100.0) },
                    )
                    WalletQuickAction(
                        label = "Cashout",
                        icon = Heroicons.Outline.ArrowDown,
                        enabled = !isBusy,
                        onClick = { onCreateTopup(300.0) },
                    )
                    WalletQuickAction(
                        label = "Send",
                        icon = Heroicons.Outline.ArrowUpRight,
                        enabled = !isBusy,
                        onClick = { onCreateTopup(500.0) },
                    )
                    WalletQuickAction(
                        label = "Transactions",
                        icon = Heroicons.Outline.QueueList,
                        enabled = !isBusy,
                        onClick = { onCreateTopup(1000.0) },
                    )
                }
            }
        }

        errorMessage?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Retry",
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(enabled = !isBusy) { onRetryClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        pendingTopup?.let {
            TopupReferencePanel(
                topup = it,
                canDismiss = selectedTopup != null,
                onDismiss = onClearSelectedTopup,
            )
        }

        requirement?.takeIf { !it.hasMinimumWalletBalance }?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Text(
                    text = "Driver Mode requires ${formatWalletAmount(it.minimumWalletBalance, it.currency)} minimum wallet balance.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WalletQuickAction(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = if (enabled) 0.17f else 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = Color.White.copy(alpha = if (enabled) 0.95f else 0.45f),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = if (enabled) 0.98f else 0.50f),
            maxLines = 1,
        )
    }
}

@Composable
private fun TopupReferencePanel(
    topup: DriverWalletTopup,
    canDismiss: Boolean,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Kiosk reference",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = topup.referenceCode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Show this at the kiosk for ${formatWalletAmount(topup.amount, topup.currency)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            if (canDismiss) {
                Text(
                    text = "Done",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TopupAmountChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (enabled) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DriverStatePill(
    isLoading: Boolean,
    status: DriverOnboardingStatus?,
) {
    val label = when {
        isLoading -> "Syncing"
        status?.canGo == true -> "Ready"
        status?.status == DriverOnboardingState.PendingReview -> "Review"
        status?.status == DriverOnboardingState.Rejected -> "Action needed"
        else -> "Finish setup"
    }
    val containerColor = when {
        status?.canGo == true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        status?.status == DriverOnboardingState.Rejected -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        status?.canGo == true -> MaterialTheme.colorScheme.primary
        status?.status == DriverOnboardingState.Rejected -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = contentColor,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TotalStatsPanel(
    isLoading: Boolean,
    stats: DriverHomeStats?,
    errorMessage: String?,
    onRetryClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardStat(
                    label = "Trips",
                    value = stats?.totalTrips?.toString() ?: "0",
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                StatSeparator()
                DashboardStat(
                    label = "Online",
                    value = stats?.onlineSeconds.formatOnlineDuration(),
                    valueColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                StatSeparator()
                RatingDashboardStat(
                    label = "Rating",
                    value = stats.ratingValueLabel(),
                    modifier = Modifier.weight(1f),
                )
            }
            when {
                isLoading -> {
                    Text(
                        text = "Updating stats...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                errorMessage != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Retry",
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onRetryClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun RatingDashboardStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val starColor = Color(0xFFF5B301)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Solid.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = starColor,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = starColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun StatSeparator() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 34.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VehicleStatusPill(vehicle: DriverVehicleInfo?) {
    val label = when {
        vehicle?.status.equals("active", ignoreCase = true) -> "Active"
        vehicle?.exists == true -> "Registered"
        else -> "Finish setup"
    }
    val isReady = vehicle?.exists == true
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isReady) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (isReady) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun DriverSetupCard(
    isLoading: Boolean,
    status: DriverOnboardingStatus?,
    errorMessage: String?,
    onSetupClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    if (isLoading && status == null && errorMessage == null) return
    if (status?.canGo == true) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = setupTitle(status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = setupDescription(status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            val setupSteps = status.setupProgressSteps()
            SetupStepper(
                steps = setupSteps,
            )

            val shouldShowAction = errorMessage != null && status == null ||
                setupSteps.any { it.status.needsDriverAction() }

            if (shouldShowAction) {
                AppPrimaryButton(
                    text = if (errorMessage != null && status == null) "Retry" else "Finish Setup",
                    onClick = if (errorMessage != null && status == null) onRetryClick else onSetupClick,
                    enabled = !isLoading,
                    height = 44.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Heroicons.Outline.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                    },
                )
            } else if (!isLoading) {
                Text(
                    text = setupReviewMessage(status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun setupTitle(status: DriverOnboardingStatus?): String {
    return when (status?.status) {
        DriverOnboardingState.PendingReview -> "Setup under review"
        DriverOnboardingState.Rejected -> "Fix setup details"
        DriverOnboardingState.Blocked -> "Setup blocked"
        else -> "Finish setup"
    }
}

private fun setupDescription(status: DriverOnboardingStatus?): String {
    return when (status?.status) {
        DriverOnboardingState.PendingReview -> "Your submitted setup is under review."
        DriverOnboardingState.Rejected -> status.blockingReasons.firstOrNull()
            ?: "Update the requested setup step before going online."
        DriverOnboardingState.Blocked -> status.blockingReasons.firstOrNull()
            ?: "Your account needs review before going online."
        else -> "Complete the required setup steps before going online."
    }
}

private fun setupReviewMessage(status: DriverOnboardingStatus?): String {
    return when (status?.status) {
        DriverOnboardingState.Rejected -> "Review the highlighted setup step when an update is requested."
        DriverOnboardingState.Blocked -> "Your account needs review before you can go online."
        else -> "Your documents are under review. Verification may take up to 3 working days, and we will notify you once your account is ready to go online."
    }
}

private fun DriverOnboardingStatus?.setupProgressSteps(): List<SetupProgressStep> {
    val stepStatuses = this?.stepStatuses
    val identityStatus = stepStatuses?.identityVerification ?: identityVerificationStatus()
    val vehicleRegistrationStatus = stepStatuses?.vehicleRegistration ?: vehicleRegistrationStatus()
    val serviceRadiusStatus = stepStatuses?.serviceRadius ?: DriverRequirementStatus.Missing

    return listOf(
        SetupProgressStep(
            label = "Account registration",
            status = stepStatuses?.accountRegistration ?: DriverRequirementStatus.Approved,
        ),
        SetupProgressStep(
            label = "Identity verification",
            status = identityStatus,
        ),
        SetupProgressStep(
            label = "Vehicle registration",
            status = vehicleRegistrationStatus,
        ),
        SetupProgressStep(
            label = "Service zone",
            status = serviceRadiusStatus,
        ),
    )
}

private fun DriverOnboardingStatus?.identityVerificationStatus(): DriverRequirementStatus {
    if (this == null) return DriverRequirementStatus.Missing

    val statuses = listOf(
        requirementStatus(DriverOnboardingDocumentType.LicenseFront),
        requirementStatus(DriverOnboardingDocumentType.LicenseBack),
        requirementStatus(DriverOnboardingDocumentType.Selfie),
    )

    return statuses.groupedStatus()
}

private fun DriverOnboardingStatus?.vehicleRegistrationStatus(): DriverRequirementStatus {
    if (this == null) return DriverRequirementStatus.Missing

    val statuses = listOf(
        requirementStatus(DriverOnboardingDocumentType.VehicleRegistration),
        requirementStatus(DriverOnboardingDocumentType.VehiclePhoto),
    )

    return statuses.groupedStatus()
}

private fun DriverOnboardingStatus?.requirementStatus(type: DriverOnboardingDocumentType): DriverRequirementStatus {
    return this?.requirements?.firstOrNull { it.type == type }?.status ?: DriverRequirementStatus.Missing
}

private fun List<DriverRequirementStatus>.groupedStatus(): DriverRequirementStatus {
    return when {
        all { it == DriverRequirementStatus.Approved } -> DriverRequirementStatus.Approved
        any { it == DriverRequirementStatus.Rejected } -> DriverRequirementStatus.Rejected
        any { it == DriverRequirementStatus.Expired } -> DriverRequirementStatus.Expired
        any { it == DriverRequirementStatus.Missing } -> DriverRequirementStatus.Missing
        any { it == DriverRequirementStatus.PendingReview } -> DriverRequirementStatus.PendingReview
        else -> DriverRequirementStatus.Uploaded
    }
}

private fun DriverRequirementStatus.needsDriverAction(): Boolean {
    return this == DriverRequirementStatus.Missing ||
        this == DriverRequirementStatus.Rejected ||
        this == DriverRequirementStatus.Expired
}

private data class SetupProgressStep(
    val label: String,
    val status: DriverRequirementStatus,
)

@Composable
private fun SetupStepper(
    steps: List<SetupProgressStep>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEach { step ->
                SetupStepperSegment(
                    step = step,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            steps.forEach { step ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = step.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = step.status.stepperStatusLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupStepperSegment(
    step: SetupProgressStep,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .height(5.dp)
            .clip(shape)
            .background(step.status.stepperColor()),
    )
}

private fun DriverRequirementStatus.stepperStatusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Missing -> "Required"
        DriverRequirementStatus.Uploaded,
        DriverRequirementStatus.PendingReview -> "Under Review"
        DriverRequirementStatus.Approved -> "Done"
        DriverRequirementStatus.Rejected -> "Fix"
        DriverRequirementStatus.Expired -> "Expired"
    }
}

private val SetupProgressStep.shortLabel: String
    get() = when (label) {
        "Account registration" -> "Account"
        "Identity verification" -> "Identity"
        "Vehicle registration" -> "Vehicle"
        "Service zone" -> "Zone"
        else -> label
    }

@Composable
private fun DriverRequirementStatus.stepperColor(): Color {
    val primary = MaterialTheme.colorScheme.primary
    val reviewYellow = Color(0xFFF9A825)
    return when (this) {
        DriverRequirementStatus.Approved -> primary.copy(alpha = 0.72f)
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> reviewYellow.copy(alpha = 0.76f)
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> primary.copy(alpha = 0.34f)
        DriverRequirementStatus.Missing -> primary.copy(alpha = 0.16f)
    }
}

private fun DriverVehicleInfo.displayName(): String {
    return listOfNotNull(make, model)
        .joinToString(" ")
        .ifBlank { vehicleTypeCode.orEmpty().replaceFirstChar { it.uppercase() } }
}

private fun DriverVehicleInfo.detailLine(): String {
    val plateText = plate?.takeIf { it.isNotBlank() }?.let { "Plate $it" }
    val yearText = year?.toString()
    val capacityText = passengerCapacity?.let { "$it seats" }
    return listOfNotNull(plateText, yearText, capacityText).joinToString(" / ")
}

private fun formatWalletAmount(amount: Double, currency: String): String {
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

private fun Long?.formatOnlineDuration(): String {
    val totalMinutes = ((this ?: 0L) / 60L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return when {
        hours <= 0L -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

private fun DriverHomeStats?.ratingValueLabel(): String {
    val value = this?.rating?.value ?: return "New"
    val tenths = (value * 10).roundToInt()
    val whole = tenths / 10
    val decimal = tenths % 10
    return "$whole.$decimal"
}
