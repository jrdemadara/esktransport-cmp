package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowLeftOnRectangle
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.Lifebuoy
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.Wallet
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord

@Composable
fun DriverSettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.clearLogoutState()
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
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
            item {
                SettingsProfileCard(uiState = uiState)
            }
            item {
                SettingsSection(
                    items = listOf(
                        SettingsMenuItem.Account,
                        SettingsMenuItem.DriverVerification,
                        SettingsMenuItem.Vehicle,
                    ),
                )
            }
            item {
                SettingsSection(
                    items = listOf(
                        SettingsMenuItem.Wallet,
                        SettingsMenuItem.Earnings,
                    ),
                )
            }
            item {
                SettingsSection(
                    items = listOf(
                        SettingsMenuItem.ServiceAreas,
                        SettingsMenuItem.Safety,
                        SettingsMenuItem.AppPreferences,
                    ),
                )
            }
            item {
                SettingsSection(
                    items = listOf(
                        SettingsMenuItem.HelpSupport,
                        SettingsMenuItem.Legal,
                    ),
                )
            }
            item {
                SettingsSection(
                    items = listOf(SettingsMenuItem.Logout),
                    isLogoutLoading = uiState.isLoggingOut,
                    onLogoutClick = viewModel::logout,
                )
            }
            item {
                VersionCard()
            }
            uiState.errorMessage?.let { message ->
                item {
                    ErrorCard(message = message)
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
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
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun SettingsProfileCard(uiState: DriverSettingsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.User,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = uiState.name.takeIf { it.isNotBlank() }?.uppercaseFirstLetterOfEachWord() ?: "Driver",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = uiState.phone.takeIf { it.isNotBlank() } ?: "Phone not provided",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Heroicons.Outline.ShieldCheck,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Active ${uiState.role}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    items: List<SettingsMenuItem>,
    isLogoutLoading: Boolean = false,
    onLogoutClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            items.forEachIndexed { index, item ->
                SettingsRow(
                    item = item,
                    isLoading = item == SettingsMenuItem.Logout && isLogoutLoading,
                    onClick = {
                        if (item == SettingsMenuItem.Logout) {
                            onLogoutClick()
                        }
                    },
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    item: SettingsMenuItem,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftIcon(
            icon = item.icon,
            backgroundColor = item.color.copy(alpha = 0.12f),
            iconColor = item.color,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onClick, enabled = item == SettingsMenuItem.Logout) {
                Icon(
                    imageVector = Heroicons.Outline.ChevronRight,
                    contentDescription = item.title,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun VersionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftIcon(
                icon = Heroicons.Outline.InformationCircle,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(7.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = "Latest",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
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
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = iconColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private enum class SettingsMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
) {
    Account(
        title = "Account",
        subtitle = "Personal information and account details",
        icon = Heroicons.Outline.User,
        color = Color(0xFF0B6BFF),
    ),
    DriverVerification(
        title = "Driver Verification",
        subtitle = "Identity, documents and approval status",
        icon = Heroicons.Outline.ShieldCheck,
        color = Color(0xFF13A85B),
    ),
    Vehicle(
        title = "Vehicle",
        subtitle = "Manage vehicle information",
        icon = Heroicons.Outline.Truck,
        color = Color(0xFF7C3AED),
    ),
    Wallet(
        title = "Wallet",
        subtitle = "Balance, top-up and transactions",
        icon = Heroicons.Outline.Wallet,
        color = Color(0xFF0B6BFF),
    ),
    Earnings(
        title = "Earnings",
        subtitle = "Earnings summary and history",
        icon = Heroicons.Outline.ChartBarSquare,
        color = Color(0xFFF59E0B),
    ),
    ServiceAreas(
        title = "Service Areas",
        subtitle = "Manage areas where you drive",
        icon = Heroicons.Outline.MapPin,
        color = Color(0xFF13A85B),
    ),
    Safety(
        title = "Safety",
        subtitle = "Emergency contacts and safety tools",
        icon = Heroicons.Outline.ShieldCheck,
        color = Color(0xFFE53935),
    ),
    AppPreferences(
        title = "App Preferences",
        subtitle = "Theme and app experience",
        icon = Heroicons.Outline.AdjustmentsHorizontal,
        color = Color(0xFF7C3AED),
    ),
    HelpSupport(
        title = "Help & Support",
        subtitle = "Get help and contact support",
        icon = Heroicons.Outline.Lifebuoy,
        color = Color(0xFFF59E0B),
    ),
    Legal(
        title = "Legal",
        subtitle = "Terms of service and privacy policy",
        icon = Heroicons.Outline.DocumentText,
        color = Color(0xFF64748B),
    ),
    Logout(
        title = "Logout",
        subtitle = "Sign out from your driver account",
        icon = Heroicons.Outline.ArrowLeftOnRectangle,
        color = Color(0xFFE53935),
    ),
}
