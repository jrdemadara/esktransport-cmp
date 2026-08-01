package org.noztek.esktransport.feature.passenger.settings.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowLeftOnRectangle
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Envelope
import com.composables.icons.heroicons.outline.LockClosed
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.User
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.common.logout.presentation.LogoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    sessionManager: SessionManager = koinInject(),
    logoutViewModel: LogoutViewModel = koinViewModel(),
) {
    val name by sessionManager.userName.collectAsState(initial = "")
    val phone by sessionManager.userPhone.collectAsState(initial = "")
    val userId by sessionManager.userId.collectAsState(initial = null)
    val logoutState by logoutViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(logoutState.isLoggedOut) {
        if (logoutState.isLoggedOut) {
            logoutViewModel.resetState()
            onLogout()
        }
    }

    LaunchedEffect(logoutState.error) {
        val message = logoutState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        topBar = { AccountTopBar(onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            AccountProfileHeader(
                name = name.orEmpty(),
                passengerId = userId,
            )
            SettingsSectionDivider(modifier = Modifier.padding(top = 22.dp, bottom = 22.dp))
            AccountSectionTitle("Personal Information")
            AccountInfoRow(
                icon = Heroicons.Outline.User,
                title = "Full Name",
                value = name.orEmpty().capitalizedDisplayValue(),
                valueFontWeight = FontWeight.SemiBold,
                showChevron = false,
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.Phone,
                title = "Phone Number",
                value = phone.orEmpty().displayValue(),
                showChevron = false,
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.Envelope,
                title = "Email Address",
                value = "Not provided",
                showChevron = false,
            )
            SettingsSectionDivider()
            AccountInfoRow(
                icon = Heroicons.Outline.MapPin,
                title = "Address",
                value = "Not provided",
                showChevron = false,
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
                isLoading = logoutState.isLoading,
                onClick = logoutViewModel::logout,
            )
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
private fun AccountProfileHeader(
    name: String,
    passengerId: Long?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.User,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = name.takeIf { it.isNotBlank() }?.uppercaseFirstLetterOfEachWord() ?: "Passenger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = passengerId.passengerIdLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            ActivePassengerPill()
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
private fun ActivePassengerPill() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE3F7EA),
        contentColor = Color(0xFF12A150),
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
                text = "Active Passenger",
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
    valueFontWeight: FontWeight = FontWeight.Normal,
    showChevron: Boolean = true,
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
            fontWeight = valueFontWeight,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showChevron) {
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

private fun String.capitalizedDisplayValue(): String {
    return takeIf { it.isNotBlank() }?.uppercaseFirstLetterOfEachWord() ?: "Not provided"
}

private fun Long?.passengerIdLabel(): String {
    return this?.let { "Passenger ID: PSG-${it.toString().padStart(4, '0')}" } ?: "Passenger ID: Not assigned"
}
