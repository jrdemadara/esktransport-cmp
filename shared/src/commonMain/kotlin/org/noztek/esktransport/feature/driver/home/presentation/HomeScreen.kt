package org.noztek.esktransport.feature.driver.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import asktransport_cmp.shared.generated.resources.Res
import asktransport_cmp.shared.generated.resources.home_car
import asktransport_cmp.shared.generated.resources.home_scooter
import asktransport_cmp.shared.generated.resources.home_tricycle
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.ExclamationTriangle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.ui.composables.driver.DriverTopBar
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleInfo
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus

enum class DriverHomeVehicleType {
    Motorcycle,
    Tricycle,
    Car,
}

@Composable
fun HomeScreen(
    todaysEarning: String = "PHP 0.00",
    totalTrips: Int = 0,
    onlineTime: String = "0h 00m",
    ratingLabel: String = "New",
    vehicleType: DriverHomeVehicleType = DriverHomeVehicleType.Motorcycle,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    onSetupClick: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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
            EarningsSummaryCard(
                todaysEarning = todaysEarning,
                totalTrips = totalTrips,
                vehicleType = vehicleType,
                status = uiState.onboardingStatus,
            )
            TodayStatsPanel(
                totalTrips = totalTrips,
                onlineTime = onlineTime,
                ratingLabel = ratingLabel,
            )
            uiState.onboardingStatus?.vehicle?.takeIf { it.exists }?.let { vehicle ->
                VehicleSummaryPanel(vehicle = vehicle)
            }
            DriverSetupCard(
                isLoading = uiState.isLoadingSetup,
                status = uiState.onboardingStatus,
                errorMessage = uiState.errorMessage,
                onSetupClick = onSetupClick,
                onRetryClick = viewModel::refreshOnboardingStatus,
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
private fun EarningsSummaryCard(
    todaysEarning: String,
    totalTrips: Int,
    vehicleType: DriverHomeVehicleType,
    status: DriverOnboardingStatus?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(142.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Today's Earning",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = todaysEarning,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (status?.canGo == true) {
                        "$totalTrips completed trips"
                    } else {
                        "Setup required before going online"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            Box(
                modifier = Modifier.size(width = 124.dp, height = 98.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(vehicleType.illustration),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

private val DriverHomeVehicleType.illustration: DrawableResource
    get() = when (this) {
        DriverHomeVehicleType.Motorcycle -> Res.drawable.home_scooter
        DriverHomeVehicleType.Tricycle -> Res.drawable.home_tricycle
        DriverHomeVehicleType.Car -> Res.drawable.home_car
}

@Composable
private fun TodayStatsPanel(
    totalTrips: Int,
    onlineTime: String,
    ratingLabel: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DashboardStat(
                label = "Trips",
                value = totalTrips.toString(),
                valueColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatSeparator()
            DashboardStat(
                label = "Online",
                value = onlineTime,
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            StatSeparator()
            DashboardStat(
                label = "Rating",
                value = ratingLabel,
                valueColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
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
private fun StatSeparator() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 34.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VehicleSummaryPanel(
    vehicle: DriverVehicleInfo?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Vehicle",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = vehicle?.displayName().orEmpty().ifBlank { "No vehicle added" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                VehicleStatusPill(vehicle = vehicle)
            }

            Text(
                text = vehicle?.detailLine().orEmpty().ifBlank { "Add the vehicle details used for passenger service." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
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

            val stepStatuses = status?.stepStatuses
            val identityStatus = stepStatuses?.identityVerification ?: status.identityVerificationStatus()
            val vehicleRegistrationStatus = stepStatuses?.vehicleRegistration
                ?: status.requirementStatus(DriverOnboardingDocumentType.VehicleRegistration)
            val serviceRadiusStatus = stepStatuses?.serviceRadius ?: DriverRequirementStatus.Missing
            SetupStepper(
                steps = listOf(
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
                        label = "Service radius",
                        status = serviceRadiusStatus,
                    ),
                ),
            )

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

private fun DriverOnboardingStatus?.identityVerificationStatus(): DriverRequirementStatus {
    if (this == null) return DriverRequirementStatus.Missing

    val statuses = listOf(
        requirementStatus(DriverOnboardingDocumentType.LicenseFront),
        requirementStatus(DriverOnboardingDocumentType.LicenseBack),
        requirementStatus(DriverOnboardingDocumentType.Selfie),
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
        "Service radius" -> "Radius"
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
