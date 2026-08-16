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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.ExclamationTriangle
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.XCircle
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus

@Composable
fun DriverVerificationScreen(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverVerificationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    DriverVerificationContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::load,
        onBottomBarNavigate = onBottomBarNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverVerificationContent(
    uiState: DriverVerificationUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = { VerificationTopBar(onBackClick = onBackClick) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.status == null -> {
                VerificationErrorState(
                    message = uiState.errorMessage ?: "Verification status is unavailable.",
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(20.dp),
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    VerificationSummaryCard(status = uiState.status)
                    VerificationStatusList(status = uiState.status)
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerificationTopBar(onBackClick: () -> Unit) {
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
                text = "Driver Verification",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun VerificationSummaryCard(status: DriverOnboardingStatus) {
    val color = status.status.statusColor()

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
                color = color.copy(alpha = 0.12f),
                contentColor = color,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = status.status.statusIcon(),
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
                    text = status.status.statusTitle(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = status.verificationSummaryText(),
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
private fun VerificationStatusList(status: DriverOnboardingStatus) {
    val items = listOf(
        VerificationItem(
            title = "Identity",
            subtitle = identitySubtitle(status),
            status = status.stepStatuses.identityVerification,
            icon = Heroicons.Outline.ShieldCheck,
        ),
        VerificationItem(
            title = "Vehicle Registration",
            subtitle = vehicleSubtitle(status),
            status = status.stepStatuses.vehicleRegistration,
            icon = Heroicons.Outline.Truck,
        ),
        VerificationItem(
            title = "Service Areas",
            subtitle = serviceAreaSubtitle(status),
            status = status.stepStatuses.serviceRadius,
            icon = Heroicons.Outline.MapPin,
        ),
        VerificationItem(
            title = "Services",
            subtitle = servicesSubtitle(status),
            status = servicesStatus(status),
            icon = Heroicons.Outline.DocumentText,
        ),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                VerificationRow(item = item)
                if (index < items.lastIndex) {
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
private fun VerificationRow(item: VerificationItem) {
    val color = item.status.statusColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.11f),
            contentColor = color,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
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
        VerificationPill(
            label = item.status.statusLabel(),
            color = color,
        )
    }
}

@Composable
private fun VerificationPill(
    label: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun VerificationErrorState(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onRetryClick) {
            Text("Retry")
        }
    }
}

private data class VerificationItem(
    val title: String,
    val subtitle: String,
    val status: DriverRequirementStatus,
    val icon: ImageVector,
)

private fun DriverOnboardingStatus.verificationSummaryText(): String {
    return when (status) {
        DriverOnboardingState.Ready -> "Your account is verified and ready for Driver Mode."
        DriverOnboardingState.PendingReview -> "Some documents are still under review."
        DriverOnboardingState.Rejected -> blockingReasons.firstOrNull()
            ?: "Some verification details need an update."
        DriverOnboardingState.Blocked -> blockingReasons.firstOrNull()
            ?: "Your account needs support review."
        DriverOnboardingState.Incomplete -> "Complete the required verification steps."
    }
}

private fun identitySubtitle(status: DriverOnboardingStatus): String {
    val licenseNo = status.license.licenseNo
    val expiry = status.license.licenseExpiry

    return listOfNotNull(
        licenseNo?.takeIf { it.isNotBlank() },
        expiry?.takeIf { it.isNotBlank() }?.let { "Expires $it" },
    ).joinToString(" • ").ifBlank { "License, ID photos, and selfie" }
}

private fun vehicleSubtitle(status: DriverOnboardingStatus): String {
    val vehicle = status.vehicle
    val type = vehicle.vehicleTypeCode
        ?.replace('_', ' ')
        ?.uppercaseFirstLetterOfEachWord()
    val plate = vehicle.plate?.takeIf { it.isNotBlank() }

    return listOfNotNull(type, plate).joinToString(" • ").ifBlank { "Vehicle photo and registration document" }
}

private fun serviceAreaSubtitle(status: DriverOnboardingStatus): String {
    return status.serviceZones
        .map { it.name }
        .joinToString(", ")
        .ifBlank { "No service area selected" }
}

private fun servicesSubtitle(status: DriverOnboardingStatus): String {
    return status.vehicle.services
        .filter { it.isEnabled }
        .map { it.serviceType.displayName }
        .joinToString(", ")
        .ifBlank { "No service selected" }
}

private fun servicesStatus(status: DriverOnboardingStatus): DriverRequirementStatus {
    val services = status.vehicle.services.filter { it.isEnabled }

    return when {
        services.isEmpty() -> DriverRequirementStatus.Missing
        services.any { it.status == DriverRequirementStatus.Rejected } -> DriverRequirementStatus.Rejected
        services.any { it.status == DriverRequirementStatus.PendingReview } -> DriverRequirementStatus.PendingReview
        services.any { it.status == DriverRequirementStatus.Uploaded } -> DriverRequirementStatus.Uploaded
        services.all { it.status == DriverRequirementStatus.Approved } -> DriverRequirementStatus.Approved
        else -> DriverRequirementStatus.Missing
    }
}

@Composable
private fun DriverOnboardingState.statusColor(): Color {
    return when (this) {
        DriverOnboardingState.Ready -> Color(0xFF16A34A)
        DriverOnboardingState.PendingReview -> Color(0xFFD97706)
        DriverOnboardingState.Rejected,
        DriverOnboardingState.Blocked -> MaterialTheme.colorScheme.error
        DriverOnboardingState.Incomplete -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun DriverOnboardingState.statusIcon(): ImageVector {
    return when (this) {
        DriverOnboardingState.Ready -> Heroicons.Outline.CheckCircle
        DriverOnboardingState.PendingReview -> Heroicons.Outline.Clock
        DriverOnboardingState.Rejected,
        DriverOnboardingState.Blocked -> Heroicons.Outline.XCircle
        DriverOnboardingState.Incomplete -> Heroicons.Outline.ExclamationTriangle
    }
}

private fun DriverOnboardingState.statusTitle(): String {
    return when (this) {
        DriverOnboardingState.Ready -> "Verified driver"
        DriverOnboardingState.PendingReview -> "Under review"
        DriverOnboardingState.Rejected -> "Action required"
        DriverOnboardingState.Blocked -> "Verification blocked"
        DriverOnboardingState.Incomplete -> "Verification incomplete"
    }
}

@Composable
private fun DriverRequirementStatus.statusColor(): Color {
    return when (this) {
        DriverRequirementStatus.Approved -> Color(0xFF16A34A)
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> Color(0xFFD97706)
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> MaterialTheme.colorScheme.error
        DriverRequirementStatus.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun DriverRequirementStatus.statusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Approved -> "Done"
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> "Review"
        DriverRequirementStatus.Rejected -> "Rejected"
        DriverRequirementStatus.Expired -> "Expired"
        DriverRequirementStatus.Missing -> "Required"
    }
}
