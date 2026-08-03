package org.noztek.esktransport.feature.driver.onboarding.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Key
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Truck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleInfo
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleService
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType

@Composable
fun DriverVehicleServicesScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverOnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    DriverVehicleServicesContent(
        state = state,
        onBack = onBack,
        onServiceToggle = viewModel::toggleVehicleService,
        onContinue = { viewModel.submitVehicleServices(onSuccess = onContinue) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverVehicleServicesContent(
    state: DriverOnboardingUiState,
    onBack: () -> Unit,
    onServiceToggle: (DriverVehicleServiceType) -> Unit,
    onContinue: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Vehicle Services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading && state.status == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            VehicleServicesHeader()
            VehicleServicesVehicleSummary(vehicle = state.status?.vehicle)
            VehicleServicesList(
                selectedServices = state.selectedVehicleServices,
                existingServices = state.status?.vehicle?.services.orEmpty(),
                onServiceToggle = onServiceToggle,
            )
            AppPrimaryButton(
                text = if (state.isSubmittingVehicleServices) "Saving..." else "Continue",
                onClick = onContinue,
                enabled = !state.isSubmittingVehicleServices,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
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

@Composable
private fun VehicleServicesHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Step 4 of 5",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
            Text(
                text = "Choose vehicle services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "City Ride lets you use Driver Mode. Rental and cargo requests are reviewed before appearing in the marketplace.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun VehicleServicesVehicleSummary(vehicle: DriverVehicleInfo?) {
    if (vehicle?.exists != true) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Truck,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.displayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOfNotNull(vehicle.vehicleTypeCode?.replaceFirstChar { it.uppercase() }, vehicle.plate)
                        .joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VehicleServicesList(
    selectedServices: Set<DriverVehicleServiceType>,
    existingServices: List<DriverVehicleService>,
    onServiceToggle: (DriverVehicleServiceType) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Available services",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            availableServices().forEach { option ->
                VehicleServiceRow(
                    option = option,
                    isSelected = option.type in selectedServices,
                    existingService = existingServices.firstOrNull { it.serviceType == option.type },
                    onClick = { onServiceToggle(option.type) },
                )
            }
        }
    }
}

@Composable
private fun VehicleServiceRow(
    option: VehicleServiceOption,
    isSelected: Boolean,
    existingService: DriverVehicleService?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
            )
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = option.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    existingService?.let {
                        VehicleServiceStatusPill(service = it)
                    }
                }
                Text(
                    text = option.description,
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
private fun VehicleServiceStatusPill(service: DriverVehicleService) {
    val (label, color) = when {
        !service.isEnabled -> "Disabled" to MaterialTheme.colorScheme.onSurfaceVariant
        service.status == DriverRequirementStatus.Approved -> "Done" to MaterialTheme.colorScheme.primary
        service.status == DriverRequirementStatus.PendingReview ||
            service.status == DriverRequirementStatus.Uploaded -> "Under review" to Color(0xFFD99A00)
        service.status == DriverRequirementStatus.Rejected -> "Fix" to MaterialTheme.colorScheme.error
        service.status == DriverRequirementStatus.Expired -> "Expired" to MaterialTheme.colorScheme.error
        else -> "Required" to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private data class VehicleServiceOption(
    val type: DriverVehicleServiceType,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private fun availableServices(): List<VehicleServiceOption> {
    val cityRide = VehicleServiceOption(
        type = DriverVehicleServiceType.Ride,
        title = "City Ride",
        description = "Short passenger trips with Driver Mode.",
        icon = Heroicons.Outline.MapPin,
    )
    val rental = VehicleServiceOption(
        type = DriverVehicleServiceType.Rental,
        title = "Vehicle Rental",
        description = "Scheduled passenger rentals reviewed by the vehicle owner.",
        icon = Heroicons.Outline.Key,
    )
    val cargo = VehicleServiceOption(
        type = DriverVehicleServiceType.Cargo,
        title = "Cargo / Delivery",
        description = "Bulk loads, delivery, and transport requests.",
        icon = Heroicons.Outline.Truck,
    )

    return listOf(cityRide, rental, cargo)
}

private fun DriverVehicleInfo.displayLabel(): String {
    val name = listOfNotNull(make, model)
        .joinToString(" ")
        .trim()

    return name.ifBlank {
        vehicleTypeCode?.replaceFirstChar { it.uppercase() } ?: "Service vehicle"
    }
}
