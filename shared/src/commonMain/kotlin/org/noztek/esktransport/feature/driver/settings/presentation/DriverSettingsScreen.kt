package org.noztek.esktransport.feature.driver.settings.presentation

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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.Lifebuoy
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.QuestionMarkCircle
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Star
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.Wallet
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.platform.AppBuildInfo
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage

@Composable
fun DriverSettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverSettingsViewModel = koinViewModel(),
    appBuildInfo: AppBuildInfo = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.clearLogoutState()
            onLogout()
        }
    }

    Scaffold(
        topBar = { SettingsTopBar(onBackClick = onBackClick) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
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
                .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 6.dp),
        ) {
            SettingsProfileRow(uiState = uiState)
            SettingsDivider()
            settingsMenuItems.forEachIndexed { index, item ->
                SettingsMenuRow(item = item)
                if (index < settingsMenuItems.lastIndex) {
                    SettingsDivider()
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SettingsFooter(versionName = appBuildInfo.versionName)
        }
    }
}

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
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
                    modifier = Modifier.size(22.dp),
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
private fun SettingsProfileRow(uiState: DriverSettingsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (uiState.profilePhotoBytes != null) {
                    CapturedDocumentPreviewImage(
                        bytes = uiState.profilePhotoBytes,
                        contentDescription = "Driver profile photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Heroicons.Outline.User,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
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
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = uiState.driverId.driverIdLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsMenuRow(
    item: SettingsMenuItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
}

@Composable
private fun SettingsFooter(versionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 10.dp),
    ) {
        settingsFooterItems(versionName).forEach { item ->
            SettingsFooterRow(item = item)
        }
    }
}

@Composable
private fun SettingsFooterRow(item: SettingsFooterItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
        )
        Text(
            text = item.title,
            modifier = if (item.value == null) Modifier.weight(1f) else Modifier,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        item.value?.let { value ->
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        }
    }
}

private enum class SettingsMenuItem(
    val title: String,
    val icon: ImageVector,
) {
    Account("Account", Heroicons.Outline.User),
    DriverVerification("Driver Verification", Heroicons.Outline.ShieldCheck),
    Vehicle("Vehicle", Heroicons.Outline.Truck),
    Wallet("Wallet", Heroicons.Outline.Wallet),
    Earnings("Earnings", Heroicons.Outline.ChartBarSquare),
    ServiceAreas("Service Areas", Heroicons.Outline.MapPin),
    Safety("Safety", Heroicons.Outline.ShieldCheck),
    AppPreferences("App Preferences", Heroicons.Outline.AdjustmentsHorizontal),
}

private data class SettingsFooterItem(
    val title: String,
    val icon: ImageVector,
    val value: String? = null,
)

private val settingsMenuItems = listOf(
    SettingsMenuItem.Account,
    SettingsMenuItem.DriverVerification,
    SettingsMenuItem.Vehicle,
    // SettingsMenuItem.Wallet,
    // SettingsMenuItem.Earnings,
    SettingsMenuItem.ServiceAreas,
    SettingsMenuItem.Safety,
    SettingsMenuItem.AppPreferences,
)

private fun settingsFooterItems(versionName: String) = listOf(
    SettingsFooterItem("Rate", Heroicons.Outline.Star),
    SettingsFooterItem("Help and tips", Heroicons.Outline.Lifebuoy),
    SettingsFooterItem("Terms and condition", Heroicons.Outline.DocumentText),
    SettingsFooterItem("Privacy policy", Heroicons.Outline.ShieldCheck),
    SettingsFooterItem("About", Heroicons.Outline.QuestionMarkCircle),
    SettingsFooterItem("Version", Heroicons.Outline.InformationCircle, versionName),
)

private fun Long?.driverIdLabel(): String {
    return this?.let { "Driver ID: DRV-${it.toString().padStart(6, '0')}" } ?: "Driver ID: -"
}
