package org.noztek.esktransport.feature.driver.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowDown
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.PlusCircle
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.outline.Wallet
import com.composables.icons.heroicons.solid.Star
import com.composables.icons.heroicons.solid.Wallet
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.blue_sedan
import esktransport.shared.generated.resources.driver_main_card_background
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.ui.composables.driver.DriverTopBar
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.feature.driver.home.domain.model.DriverHomeStats
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleInfo
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTrip
import org.noztek.esktransport.feature.driver.trips.domain.model.DriverTripStatus
import org.noztek.esktransport.feature.driver.wallet.domain.model.DriverWalletDashboard
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    statsRefreshToken: Long = 0L,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    onSetupClick: (DriverOnboardingStatus?) -> Unit = {},
    onDriverModeClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onCashoutClick: () -> Unit = {},
    onTransactionHistoryClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
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
                viewModel.refreshEarnings(showLoading = false)
                viewModel.refreshRecentActivity(showLoading = false)
                viewModel.refreshProfilePhoto()
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
            viewModel.refreshEarnings(showLoading = false)
            viewModel.refreshRecentActivity(showLoading = false)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            DriverTopBar(
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                greetingName = uiState.userName,
                profilePhotoBytes = uiState.profilePhotoBytes,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DriverMainStatusCard(
                isLoading = uiState.isLoadingSetup,
                status = uiState.onboardingStatus,
                stats = uiState.stats,
                todaysEarning = uiState.earningsDashboard?.today?.netEarning,
                earningsCurrency = uiState.earningsDashboard?.currency ?: "PHP",
                walletDashboard = uiState.walletDashboard,
                onDriverModeClick = onDriverModeClick,
                onSetupClick = { onSetupClick(uiState.onboardingStatus) },
            )
            DriverWalletStrip(
                isLoading = uiState.isLoadingWallet,
                dashboard = uiState.walletDashboard,
                errorMessage = uiState.walletErrorMessage,
                onTopUpClick = onTopUpClick,
                onCashoutClick = onCashoutClick,
                onHistoryClick = onTransactionHistoryClick,
                onRetryClick = viewModel::refreshWallet,
            )
            PerformancePanel(
                isLoading = uiState.isLoadingStats,
                stats = uiState.stats,
                errorMessage = uiState.statsErrorMessage,
                onRetryClick = viewModel::refreshStats,
            )
            DriverQuickActions(
                onTripsClick = { onBottomBarNavigate(DriverBottomBarRoute.TRIPS) },
                onEarningsClick = { onBottomBarNavigate(DriverBottomBarRoute.EARNINGS) },
                onSupportClick = onProfileClick,
                onVehicleClick = { onSetupClick(uiState.onboardingStatus) },
            )
            RecentActivityCard(
                isLoading = uiState.isLoadingRecentActivity,
                trip = uiState.recentActivityTrip,
                routePoints = uiState.recentActivityRoutePoints,
                errorMessage = uiState.recentActivityErrorMessage,
                mapboxConfig = mapboxConfig,
                onSeeAllClick = { onBottomBarNavigate(DriverBottomBarRoute.TRIPS) },
                onRetryClick = viewModel::refreshRecentActivity,
            )

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
private fun DriverMainStatusCard(
    isLoading: Boolean,
    status: DriverOnboardingStatus?,
    stats: DriverHomeStats?,
    todaysEarning: Double?,
    earningsCurrency: String,
    walletDashboard: DriverWalletDashboard?,
    onDriverModeClick: () -> Unit,
    onSetupClick: () -> Unit,
) {
    val canGo = status?.canGo == true
    val walletRequirement = walletDashboard?.driverModeRequirement
    val hasRequiredBalance = walletRequirement?.hasMinimumWalletBalance != false
    val canOpenDriverMode = canGo && hasRequiredBalance
    val todaysEarningLabel = formatWalletAmount(todaysEarning ?: 0.0, earningsCurrency)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF075BE8),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
        ) {
            Image(
                painter = painterResource(Res.drawable.driver_main_card_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = 1.02f,
                        scaleY = 1.02f,
                    ),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F6BF2).copy(alpha = 0.16f),
                                Color(0xFF0649C7).copy(alpha = 0.70f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        DriverStatusPill(
                            isLoading = isLoading,
                            canGo = canGo,
                            status = status,
                        )
                        DriverRatingPill(rating = stats.ratingValueLabel())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = if (canGo) "Ready to drive" else "Setup required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = when {
                                    canGo && hasRequiredBalance -> "Switch to Driver Mode \nwhen you are ready."
                                    canGo -> "Top up your wallet to start accepting rides."
                                    else -> "Finish verification before going online."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.86f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            DriverMainActionButton(
                                text = if (canGo) "Driver Mode" else "Finish Setup",
                                enabled = if (canGo) canOpenDriverMode else !isLoading,
                                onClick = if (canGo) onDriverModeClick else onSetupClick,
                            )
                        }
                        Image(
                            painter = painterResource(Res.drawable.blue_sedan),
                            contentDescription = null,
                            modifier = Modifier
                                .width(190.dp)
                                .offset(y = 2.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                if (canGo && walletRequirement != null && !hasRequiredBalance) {
                    Text(
                        text = "Minimum wallet balance: ${formatWalletAmount(walletRequirement.minimumWalletBalance, walletRequirement.currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xFF053DAD).copy(alpha = 0.74f),
                    contentColor = Color.White,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MainMetric(
                            icon = Heroicons.Outline.Plus,
                            value = todaysEarningLabel,
                            label = "Earned today",
                            modifier = Modifier.weight(1f),
                        )
                        LightStatSeparator()
                        MainMetric(
                            icon = Heroicons.Outline.QueueList,
                            value = stats?.totalTrips?.toString() ?: "0",
                            label = "Trips",
                            modifier = Modifier.weight(0.78f),
                        )
                        LightStatSeparator()
                        MainMetric(
                            icon = Heroicons.Outline.Clock,
                            value = stats?.onlineSeconds.formatOnlineDuration(),
                            label = "Online",
                            modifier = Modifier.weight(0.85f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRatingPill(
    rating: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.18f),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Solid.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFFFC44D),
            )
            Text(
                text = rating,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DriverStatusPill(
    isLoading: Boolean,
    canGo: Boolean,
    status: DriverOnboardingStatus?,
) {
    val label = when {
        isLoading -> "SYNCING"
        canGo -> "READY"
        status?.status == DriverOnboardingState.PendingReview -> "UNDER REVIEW"
        status?.status == DriverOnboardingState.Rejected -> "ACTION NEEDED"
        else -> "REQUIRED"
    }
    val dotColor = when {
        canGo -> Color(0xFF22C55E)
        status?.status == DriverOnboardingState.PendingReview -> Color(0xFFFACC15)
        status?.status == DriverOnboardingState.Rejected -> Color(0xFFFF6B6B)
        else -> Color.White.copy(alpha = 0.72f)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.92f),
        contentColor = Color(0xFF0757D8),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DriverMainActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = if (enabled) 0.96f else 0.56f),
        contentColor = Color(0xFF075BE8),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@Composable
private fun MainMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconTint,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LightStatSeparator() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(width = 1.dp, height = 34.dp)
            .background(Color.White.copy(alpha = 0.20f)),
    )
}

@Composable
private fun DriverWalletStrip(
    isLoading: Boolean,
    dashboard: DriverWalletDashboard?,
    errorMessage: String?,
    onTopUpClick: () -> Unit,
    onCashoutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val balanceLabel = dashboard?.wallet?.let { formatWalletAmount(it.balance, it.currency) } ?: "PHP 0.00"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 0.5.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Heroicons.Solid.Wallet,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = balanceLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(width = 1.dp, height = 58.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WalletQuickAction(
                        label = "Top Up",
                        icon = Heroicons.Outline.Plus,
                        color = MaterialTheme.colorScheme.primary,
                        enabled = !isLoading,
                        onClick = onTopUpClick,
                    )
                    WalletQuickAction(
                        label = "Cashout",
                        icon = Heroicons.Outline.ArrowDown,
                        color = Color(0xFF21B36B),
                        enabled = !isLoading,
                        onClick = onCashoutClick,
                    )
                    //todo:future implementation
//                    WalletQuickAction(
//                        label = "Send",
//                        icon = Heroicons.Outline.PaperAirplane,
//                        color = MaterialTheme.colorScheme.secondary,
//                        enabled = !isBusy,
//                        onClick = {},
//                    )
                    WalletQuickAction(
                        label = "History",
                        icon = Heroicons.Outline.QueueList,
                        color = MaterialTheme.colorScheme.tertiary,
                        enabled = !isLoading,
                        onClick = onHistoryClick,
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
                            .clickable(enabled = !isLoading) { onRetryClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverModeRequirementMessage(
    minimumBalance: Double,
    currency: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = "Driver Mode requires ${formatWalletAmount(minimumBalance, currency)} minimum wallet balance.",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WalletQuickAction(
    label: String,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
private fun PerformancePanel(
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
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Today's Performance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "View details",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onRetryClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PerformanceMetric(
                    icon = Heroicons.Outline.QueueList,
                    iconColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    value = stats?.totalTrips?.toString() ?: "0",
                    label = "Trips",
                    modifier = Modifier.weight(1f),
                )
                StatSeparator()
                PerformanceMetric(
                    icon = Heroicons.Outline.Clock,
                    iconColor = Color(0xFF20B66A),
                    containerColor = Color(0xFF20B66A).copy(alpha = 0.12f),
                    value = stats?.onlineSeconds.formatOnlineDuration(),
                    label = "Online",
                    modifier = Modifier.weight(1f),
                )
                StatSeparator()
                PerformanceMetric(
                    icon = Heroicons.Solid.Star,
                    iconColor = Color(0xFFF5B301),
                    containerColor = Color(0xFFF5B301).copy(alpha = 0.16f),
                    value = stats.ratingValueLabel(),
                    label = "Rating",
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
private fun PerformanceMetric(
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(containerColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(21.dp),
                tint = iconColor,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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
}

@Composable
private fun DriverQuickActions(
    onTripsClick: () -> Unit,
    onEarningsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onVehicleClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DriverQuickActionTile(
            title = "Trips",
            subtitle = "History",
            icon = Heroicons.Outline.QueueList,
            iconColor = MaterialTheme.colorScheme.primary,
            onClick = onTripsClick,
            modifier = Modifier.weight(1f),
        )
        DriverQuickActionTile(
            title = "Earnings",
            subtitle = "Income",
            icon = Heroicons.Outline.ChartBarSquare,
            iconColor = Color(0xFF16A661),
            onClick = onEarningsClick,
            modifier = Modifier.weight(1f),
        )
        DriverQuickActionTile(
            title = "Support",
            subtitle = "Help",
            icon = Heroicons.Outline.PaperAirplane,
            iconColor = Color(0xFF7C3AED),
            onClick = onSupportClick,
            modifier = Modifier.weight(1f),
        )
        DriverQuickActionTile(
            title = "Vehicle",
            subtitle = "Manage",
            icon = Heroicons.Outline.MapPin,
            iconColor = Color(0xFFF59E0B),
            onClick = onVehicleClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DriverQuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconColor,
            )
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    isLoading: Boolean,
    trip: DriverTrip?,
    routePoints: List<MapPoint>,
    errorMessage: String?,
    mapboxConfig: MapboxConfig,
    onSeeAllClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "See all",
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onSeeAllClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            when {
                isLoading && trip == null -> RecentActivityLoading()
                errorMessage != null && trip == null -> RecentActivityError(
                    message = errorMessage,
                    onRetryClick = onRetryClick,
                )
                trip == null -> RecentActivityEmpty()
                else -> RecentActivityTripRow(
                    trip = trip,
                    routePoints = routePoints,
                    mapboxConfig = mapboxConfig,
                )
            }
        }
    }
}

@Composable
private fun RecentActivityTripRow(
    trip: DriverTrip,
    routePoints: List<MapPoint>,
    mapboxConfig: MapboxConfig,
) {
    val fareLabel = trip.recentFareLabel()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecentTripMapPreview(
            routePoints = routePoints,
            mapboxConfig = mapboxConfig,
            modifier = Modifier
                .size(width = 118.dp, height = 76.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = trip.passengerName.takeIf { it.isNotBlank() } ?: "Last trip",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = trip.routeLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = trip.activityTimestamp().formatApiDateTimeForDisplay(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            trip.distanceTimeLabel()?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = fareLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            RecentActivityStatusPill(status = trip.status)
        }
    }
}

@Composable
private fun RecentTripMapPreview(
    routePoints: List<MapPoint>,
    mapboxConfig: MapboxConfig,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outlineVariant

    if (routePoints.size < 2) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.MapPin,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        return
    }

    PlatformMapView(
        modifier = modifier.border(1.dp, outline.copy(alpha = 0.38f), RoundedCornerShape(12.dp)),
        config = mapboxConfig,
        cameraCenter = routePoints.mapCenter(),
        cameraDefaults = MapCameraDefaults(zoom = routePoints.recentMapZoom()),
        routeLines = listOf(
            MapRouteLine(
                id = "recent-route-glow",
                points = routePoints,
                color = primary,
                width = 7.0,
                opacity = 0.18,
            ),
            MapRouteLine(
                id = "recent-route",
                points = routePoints,
                color = primary,
                width = 3.5,
                opacity = 0.95,
            ),
        ),
    )
}

@Composable
private fun RecentActivityStatusPill(status: DriverTripStatus) {
    val (label, container, content) = when (status) {
        DriverTripStatus.Completed -> Triple("Completed", Color(0xFFE7F8EE), Color(0xFF139650))
        DriverTripStatus.Cancelled -> Triple("Cancelled", Color(0xFFFFF1E7), Color(0xFFE45514))
        DriverTripStatus.Expired -> Triple("Expired", Color(0xFFFFF7E6), Color(0xFFB77900))
        DriverTripStatus.InProgress,
        DriverTripStatus.ArrivingPickup,
        DriverTripStatus.Accepted,
        DriverTripStatus.Offered -> Triple("Ongoing", MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), MaterialTheme.colorScheme.primary)
        DriverTripStatus.Unknown -> Triple("Trip", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun RecentActivityLoading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Text(
            text = "Loading recent trip",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RecentActivityError(
    message: String,
    onRetryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Retry",
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onRetryClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun RecentActivityEmpty() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.QueueList,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "No trips yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Your latest completed trip will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
        shape = RoundedCornerShape(14.dp),
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

private fun DriverTrip.routeLabel(): String {
    val pickupLabel = pickup.label?.takeIf { it.isNotBlank() } ?: "Pickup"
    val dropoffLabel = dropoff.label?.takeIf { it.isNotBlank() } ?: "Drop-off"
    return "$pickupLabel  →  $dropoffLabel"
}

private fun DriverTrip.activityTimestamp(): String? {
    return completedAt
        ?: canceledAt
        ?: pickupConfirmedAt
        ?: acceptedAt
        ?: assignedAt
        ?: requestedAt
}

private fun DriverTrip.recentFareLabel(): String {
    val fare = finalFare ?: settlement?.grossFare ?: return "-"
    return formatWalletAmount(fare, currency)
}

private fun DriverTrip.distanceTimeLabel(): String? {
    val distance = distanceKm?.takeIf { it > 0.0 }?.let { "${it.formatOneDecimal()} km" }
    val duration = durationMin?.takeIf { it > 0 }?.let { "${it} min" }
    return listOfNotNull(distance, duration)
        .joinToString(" • ")
        .takeIf { it.isNotBlank() }
}

private fun List<MapPoint>.mapCenter(): MapPoint {
    return MapPoint(
        latitude = sumOf { it.latitude } / size,
        longitude = sumOf { it.longitude } / size,
    )
}

private fun List<MapPoint>.recentMapZoom(): Double {
    if (size < 2) return 14.0

    val latSpan = maxOf { it.latitude } - minOf { it.latitude }
    val lngSpan = maxOf { it.longitude } - minOf { it.longitude }
    val maxSpan = maxOf(abs(latSpan), abs(lngSpan))

    return when {
        maxSpan < 0.01 -> 14.2
        maxSpan < 0.025 -> 13.2
        maxSpan < 0.06 -> 12.0
        maxSpan < 0.12 -> 11.0
        else -> 10.0
    }
}

private fun Double.formatOneDecimal(): String {
    val tenths = (this * 10).roundToInt()
    val whole = tenths / 10
    val decimal = abs(tenths % 10)
    return "$whole.$decimal"
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
