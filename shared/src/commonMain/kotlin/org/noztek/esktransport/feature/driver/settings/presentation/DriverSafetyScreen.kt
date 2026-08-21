package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.ExclamationTriangle
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.ShieldCheck
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute

@Composable
fun DriverSafetyScreen(
    onBackClick: () -> Unit,
    onLocationSharingClick: () -> Unit = {},
    onEmergencyContactsClick: () -> Unit = {},
    onReportIncidentClick: () -> Unit = {},
    onSafetyTipsClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
) {
    DriverSafetyContent(
        onBackClick = onBackClick,
        onLocationSharingClick = onLocationSharingClick,
        onEmergencyContactsClick = onEmergencyContactsClick,
        onReportIncidentClick = onReportIncidentClick,
        onSafetyTipsClick = onSafetyTipsClick,
        onBottomBarNavigate = onBottomBarNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverSafetyContent(
    onBackClick: () -> Unit,
    onLocationSharingClick: () -> Unit,
    onEmergencyContactsClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onSafetyTipsClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = { SafetyTopBar(onBackClick = onBackClick) },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SafetyStatusCard()
            SafetyMenuList(
                onLocationSharingClick = onLocationSharingClick,
                onEmergencyContactsClick = onEmergencyContactsClick,
                onReportIncidentClick = onReportIncidentClick,
                onSafetyTipsClick = onSafetyTipsClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafetyTopBar(onBackClick: () -> Unit) {
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
                text = "Safety",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun SafetyStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.ShieldCheck,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Driver safety tools",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Manage emergency contacts, reports, and trip safety settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SafetyMenuList(
    onLocationSharingClick: () -> Unit,
    onEmergencyContactsClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onSafetyTipsClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            safetyMenuItems.forEachIndexed { index, item ->
                SafetyMenuRow(
                    item = item,
                    onClick = {
                        when (item.type) {
                            SafetyMenuItemType.LocationSharing -> onLocationSharingClick()
                            SafetyMenuItemType.EmergencyContacts -> onEmergencyContactsClick()
                            SafetyMenuItemType.ReportIncident -> onReportIncidentClick()
                            SafetyMenuItemType.SafetyTips -> onSafetyTipsClick()
                        }
                    },
                )
                if (index < safetyMenuItems.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                        modifier = Modifier.padding(start = 58.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyMenuRow(
    item: SafetyMenuItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
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
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class SafetyMenuItem(
    val type: SafetyMenuItemType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

private enum class SafetyMenuItemType {
    LocationSharing,
    EmergencyContacts,
    ReportIncident,
    SafetyTips,
}

private val safetyMenuItems = listOf(
    SafetyMenuItem(
        type = SafetyMenuItemType.LocationSharing,
        title = "Location Sharing",
        subtitle = "Trip location visibility and safety status",
        icon = Heroicons.Outline.MapPin,
    ),
    SafetyMenuItem(
        type = SafetyMenuItemType.EmergencyContacts,
        title = "Emergency Contacts",
        subtitle = "People to contact during urgent situations",
        icon = Heroicons.Outline.Phone,
    ),
    SafetyMenuItem(
        type = SafetyMenuItemType.ReportIncident,
        title = "Report an Incident",
        subtitle = "Send a trip, passenger, or payment concern",
        icon = Heroicons.Outline.ExclamationTriangle,
    ),
    SafetyMenuItem(
        type = SafetyMenuItemType.SafetyTips,
        title = "Safety Tips",
        subtitle = "Pickup, cash handling, and trip reminders",
        icon = Heroicons.Outline.InformationCircle,
    ),
)
