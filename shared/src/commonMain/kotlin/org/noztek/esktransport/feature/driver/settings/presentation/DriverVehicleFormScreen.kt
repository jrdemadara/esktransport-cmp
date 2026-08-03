package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Key
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Truck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVehicleFormScreen(
    vehiclePublicId: String?,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverVehicleFormViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vehiclePublicId) {
        viewModel.load(vehiclePublicId)
    }

    Scaffold(
        topBar = {
            VehicleFormTopBar(
                title = if (vehiclePublicId == null) "Add Vehicle" else "Edit Vehicle",
                onBackClick = onBackClick,
            )
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val vehicleTypes = uiState.vehicleTypes.ifEmpty { fallbackVehicleTypes() }
            VehicleFormSection(title = "Vehicle type") {
                VehicleTypeOptions(
                    vehicleTypes = vehicleTypes,
                    selectedType = uiState.vehicleTypeCode,
                    onSelect = viewModel::updateVehicleType,
                )
            }
            VehicleFormSection(title = "Vehicle use") {
                VehicleUseOptions(
                    allowedServices = vehicleTypes
                        .firstOrNull { it.code == uiState.vehicleTypeCode }
                        ?.allowedServices
                        .orEmpty(),
                    selectedServices = uiState.selectedServices,
                    onToggle = viewModel::toggleService,
                )
            }
            VehicleFormSection(title = "Details") {
                AppInputField(
                    value = uiState.plate,
                    onValueChange = viewModel::updatePlate,
                    label = "Plate number",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInputField(
                        value = uiState.make,
                        onValueChange = viewModel::updateMake,
                        label = "Make",
                        modifier = Modifier.weight(1f),
                    )
                    AppInputField(
                        value = uiState.model,
                        onValueChange = viewModel::updateModel,
                        label = "Model",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInputField(
                        value = uiState.year,
                        onValueChange = viewModel::updateYear,
                        label = "Year",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    AppInputField(
                        value = uiState.passengerCapacity,
                        onValueChange = viewModel::updatePassengerCapacity,
                        label = "Seats",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            VehicleFormSection(title = "Cargo capacity") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInputField(
                        value = uiState.payloadKg,
                        onValueChange = viewModel::updatePayloadKg,
                        label = "Payload kg",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    AppInputField(
                        value = uiState.volumeM3,
                        onValueChange = viewModel::updateVolumeM3,
                        label = "Volume m3",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            AppPrimaryButton(
                text = if (uiState.isSaving) "Saving..." else "Save Vehicle",
                onClick = { viewModel.save(onSuccess = onSaved) },
                enabled = !uiState.isSaving,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun VehicleFormTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
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
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun VehicleFormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun VehicleTypeOptions(
    vehicleTypes: List<DriverVehicleType>,
    selectedType: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        vehicleTypes.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { vehicleType ->
                    FilterChip(
                        selected = selectedType == vehicleType.code,
                        onClick = { onSelect(vehicleType.code) },
                        label = { Text(vehicleType.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleUseOptions(
    allowedServices: List<DriverVehicleServiceType>,
    selectedServices: Set<DriverVehicleServiceType>,
    onToggle: (DriverVehicleServiceType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        vehicleServiceOptions()
            .filter { it.type in allowedServices }
            .forEach { option ->
            VehicleUseRow(
                option = option,
                isSelected = option.type in selectedServices,
                onClick = { onToggle(option.type) },
            )
        }
    }
}

private fun fallbackVehicleTypes(): List<DriverVehicleType> {
    return listOf(
        DriverVehicleType(
            code = "motorcycle",
            name = "Motorcycle",
            description = null,
            supportsCargo = false,
            passengerMax = 1,
            sortOrder = 10,
            allowedServices = listOf(DriverVehicleServiceType.Ride),
        ),
        DriverVehicleType(
            code = "tricycle",
            name = "Tricycle",
            description = null,
            supportsCargo = false,
            passengerMax = 3,
            sortOrder = 20,
            allowedServices = listOf(DriverVehicleServiceType.Ride, DriverVehicleServiceType.Rental),
        ),
        DriverVehicleType(
            code = "sedan",
            name = "Sedan",
            description = null,
            supportsCargo = false,
            passengerMax = 4,
            sortOrder = 30,
            allowedServices = listOf(DriverVehicleServiceType.Ride, DriverVehicleServiceType.Rental),
        ),
    )
}

@Composable
private fun VehicleUseRow(
    option: VehicleServiceOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.size(22.dp),
            )
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class VehicleServiceOption(
    val type: DriverVehicleServiceType,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private fun vehicleServiceOptions(): List<VehicleServiceOption> {
    return listOf(
        VehicleServiceOption(
            type = DriverVehicleServiceType.Ride,
            title = "City Ride",
            description = "Use this vehicle in Driver Mode.",
            icon = Heroicons.Outline.MapPin,
        ),
        VehicleServiceOption(
            type = DriverVehicleServiceType.Rental,
            title = "Rental",
            description = "Accept scheduled rental requests.",
            icon = Heroicons.Outline.Key,
        ),
        VehicleServiceOption(
            type = DriverVehicleServiceType.Cargo,
            title = "Cargo",
            description = "Accept delivery or bulk load requests.",
            icon = Heroicons.Outline.Truck,
        ),
    )
}
