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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Truck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.displayName
import org.noztek.esktransport.feature.driver.settings.domain.model.enabledServiceLabels
import org.noztek.esktransport.feature.driver.settings.domain.model.hasApprovedRideService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVehiclesScreen(
    refreshToken: Long = 0L,
    onBackClick: () -> Unit,
    onAddVehicleClick: () -> Unit = {},
    onVehicleClick: (DriverVehicle) -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverVehiclesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearStatusMessage()
    }

    LaunchedEffect(refreshToken) {
        if (refreshToken > 0L) {
            viewModel.refresh(showLoading = false)
        }
    }

    Scaffold(
        topBar = {
            VehiclesTopBar(
                onBackClick = onBackClick,
                onAddVehicleClick = onAddVehicleClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            uiState.vehicles.isEmpty() -> {
                EmptyVehiclesState(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(20.dp),
                    onAddVehicleClick = onAddVehicleClick,
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Manage the vehicles connected to your driver account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    uiState.vehicles.forEach { vehicle ->
                        DriverVehicleCard(
                            vehicle = vehicle,
                            isActivating = uiState.isActivatingVehicleId == vehicle.publicId,
                            onEditClick = { onVehicleClick(vehicle) },
                            onActivateRideClick = { viewModel.activateRideVehicle(vehicle) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiclesTopBar(
    onBackClick: () -> Unit,
    onAddVehicleClick: () -> Unit,
) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
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
                text = "Vehicles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = onAddVehicleClick) {
                Icon(
                    imageVector = Heroicons.Outline.Plus,
                    contentDescription = "Add vehicle",
                    modifier = Modifier.size(22.dp),
                )
            }
        },
    )
}

@Composable
private fun DriverVehicleCard(
    vehicle: DriverVehicle,
    isActivating: Boolean,
    onEditClick: () -> Unit,
    onActivateRideClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.Truck,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = vehicle.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            vehicle.vehicleTypeCode?.replaceFirstChar { it.uppercase() },
                            vehicle.plate,
                        ).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                VehicleStatusPill(
                    label = if (vehicle.isActiveRideVehicle) "Active ride" else vehicle.verificationStatus.statusLabel(),
                    color = if (vehicle.isActiveRideVehicle) MaterialTheme.colorScheme.primary else vehicle.verificationStatus.statusColor(),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            VehicleInfoLine(
                icon = vehicle.verificationStatus.statusIcon(),
                label = "Verification",
                value = vehicle.verificationStatus.statusLabel(),
                color = vehicle.verificationStatus.statusColor(),
            )
            VehicleInfoLine(
                icon = Heroicons.Outline.Truck,
                label = "Services",
                value = vehicle.enabledServiceLabels.ifEmpty { listOf("No services selected") }.joinToString(", "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val canActivateRide = vehicle.hasApprovedRideService() && !vehicle.isActiveRideVehicle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onEditClick) {
                    Text("View details")
                }
                if (canActivateRide) {
                    TextButton(
                        onClick = onActivateRideClick,
                        enabled = !isActivating,
                    ) {
                        Text(if (isActivating) "Setting..." else "Use for Driver Mode")
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoLine(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VehicleStatusPill(
    label: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
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
private fun EmptyVehiclesState(
    modifier: Modifier,
    onAddVehicleClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Truck,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            text = "No vehicles yet",
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Add a vehicle to prepare City Ride, rental, or cargo services.",
            modifier = Modifier.padding(top = 5.dp, bottom = 18.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppPrimaryButton(
            text = "Add Vehicle",
            onClick = onAddVehicleClick,
        )
    }
}

private fun DriverRequirementStatus.statusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Approved -> "Approved"
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> "Under review"
        DriverRequirementStatus.Rejected -> "Needs update"
        DriverRequirementStatus.Expired -> "Expired"
        DriverRequirementStatus.Missing -> "Required"
    }
}

@Composable
private fun DriverRequirementStatus.statusColor(): Color {
    return when (this) {
        DriverRequirementStatus.Approved -> MaterialTheme.colorScheme.primary
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> Color(0xFFD99A00)
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> MaterialTheme.colorScheme.error
        DriverRequirementStatus.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun DriverRequirementStatus.statusIcon(): ImageVector {
    return when (this) {
        DriverRequirementStatus.Approved -> Heroicons.Outline.CheckCircle
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> Heroicons.Outline.Clock
        else -> Heroicons.Outline.Truck
    }
}
