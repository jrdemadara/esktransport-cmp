package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowLeftOnRectangle
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.Camera
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Envelope
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.User
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage

@Composable
fun DriverAccountScreen(
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
        topBar = { AccountTopBar(onBackClick = onBackClick) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            AccountProfileHeader(uiState = uiState)
            SettingsSectionDivider(modifier = Modifier.padding(top = 22.dp, bottom = 22.dp))
            AccountSectionTitle("Personal Information")
            AccountInfoRow(
                icon = Heroicons.Outline.User,
                title = "Full Name",
                value = uiState.name.displayValue(),
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.Phone,
                title = "Phone Number",
                value = uiState.phone.displayValue(),
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.Envelope,
                title = "Email Address",
                value = "Not provided",
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.CalendarDays,
                title = "Date of Birth",
                value = "Not provided",
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.MapPin,
                title = "Address",
                value = "Not provided",
            )
            SettingsSectionDivider(modifier = Modifier.padding(top = 22.dp, bottom = 22.dp))
            AccountSectionTitle("Account Security")
            AccountInfoRow(
                icon = Heroicons.Outline.LockClosed,
                title = "Password",
                value = "••••••••",
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.ShieldCheck,
                title = "Two-Factor Authentication",
                value = "Off",
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingsSectionDivider(modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
            AccountLogoutRow(
                isLoading = uiState.isLoggingOut,
                onClick = viewModel::logout,
            )
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun AccountTopBar(onBackClick: () -> Unit) {
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
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        title = {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun AccountProfileHeader(uiState: DriverSettingsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(82.dp)) {
            Surface(
                modifier = Modifier
                    .size(76.dp)
                    .align(Alignment.TopStart),
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
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
            VerifiedDriverPill(isVerified = uiState.isVerifiedDriver)
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
private fun VerifiedDriverPill(isVerified: Boolean) {
    val background = if (isVerified) Color(0xFFE3F7EA) else MaterialTheme.colorScheme.surfaceVariant
    val content = if (isVerified) Color(0xFF12A150) else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = background,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Heroicons.Outline.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = if (isVerified) "Verified Driver" else "Verification pending",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AccountSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.padding(bottom = 12.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(23.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = title,
            modifier = Modifier.weight(0.95f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.05f),
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
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
private fun AccountLogoutRow(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.ArrowLeftOnRectangle,
                contentDescription = null,
                modifier = Modifier.size(23.dp),
                tint = Color(0xFFE53935),
            )
        }
        Text(
            text = if (isLoading) "Logging out..." else "Logout",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE53935),
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
private fun SettingsSectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
    )
}

private fun String.displayValue(): String = takeIf { it.isNotBlank() } ?: "Not provided"
