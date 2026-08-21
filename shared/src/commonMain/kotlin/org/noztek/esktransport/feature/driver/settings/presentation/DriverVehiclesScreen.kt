package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.composables.icons.heroicons.outline.ArrowUpTray
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.EllipsisVertical
import com.composables.icons.heroicons.outline.PencilSquare
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Tag
import com.composables.icons.heroicons.outline.Truck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.displayName
import org.noztek.esktransport.feature.driver.settings.domain.model.hasApprovedRideService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverVehiclesScreen(
    refreshToken: Long = 0L,
    onBackClick: () -> Unit,
    onAddVehicleClick: () -> Unit = {},
    onVehicleClick: (DriverVehicle) -> Unit = {},
    onManageListingClick: (DriverVehicle, DriverVehicleServiceType) -> Unit = { _, _ -> },
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverVehiclesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(VehicleServiceFilter.All) }

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
            else -> {
                val vehicles = uiState.vehicles.filterBy(selectedFilter)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeaderCopy()
                    AppPrimaryButton(
                        text = "Add Vehicle",
                        onClick = onAddVehicleClick,
                        height = 46.dp,
                        trailingIcon = {
                            Icon(
                                imageVector = Heroicons.Outline.Plus,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                            )
                        },
                    )
                    VehicleStatsRow(vehicles = uiState.vehicles)
                    VehicleFilterRow(
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it },
                    )
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (vehicles.isEmpty()) {
                        EmptyVehiclesState(
                            modifier = Modifier.fillMaxWidth(),
                            filter = selectedFilter,
                            onAddVehicleClick = onAddVehicleClick,
                        )
                    } else {
                        vehicles.forEach { vehicle ->
                            DriverVehicleCard(
                                vehicle = vehicle,
                                vehiclePhotoBytes = uiState.vehiclePhotoBytes[vehicle.publicId],
                                isActivating = uiState.isActivatingVehicleId == vehicle.publicId,
                                onEditClick = { onVehicleClick(vehicle) },
                                onListingClick = vehicle.listingServiceType(selectedFilter.serviceType)?.let { serviceType ->
                                    { onManageListingClick(vehicle, serviceType) }
                                },
                                onActivateRideClick = { viewModel.activateRideVehicle(vehicle) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiclesTopBar(
    onBackClick: () -> Unit,
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
        actions = {},
    )
}

@Composable
private fun HeaderCopy() {
    Text(
        text = "Manage your registered vehicles.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun VehicleStatsRow(vehicles: List<DriverVehicle>) {
    val activeCount = vehicles.count { it.isActiveRideVehicle || it.verificationStatus == DriverRequirementStatus.Approved }
    val pendingCount = vehicles.count {
        it.verificationStatus == DriverRequirementStatus.PendingReview ||
            it.verificationStatus == DriverRequirementStatus.Uploaded
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VehicleStatCard(
            icon = Heroicons.Outline.Truck,
            value = vehicles.size.toString(),
            label = "Registered",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        VehicleStatCard(
            icon = Heroicons.Outline.CheckCircle,
            value = activeCount.toString(),
            label = "Approved",
            color = Color(0xFF16A34A),
            modifier = Modifier.weight(1f),
        )
        VehicleStatCard(
            icon = Heroicons.Outline.Clock,
            value = pendingCount.toString(),
            label = "Review",
            color = Color(0xFFD97706),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun VehicleStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                contentColor = color,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VehicleFilterRow(
    selectedFilter: VehicleServiceFilter,
    onFilterChange: (VehicleServiceFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VehicleServiceFilter.entries.forEach { filter ->
            FilterPill(
                text = filter.label,
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
            )
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = Modifier
            .height(34.dp)
            .border(1.dp, color.copy(alpha = if (selected) 0f else 0.75f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DriverVehicleCard(
    vehicle: DriverVehicle,
    vehiclePhotoBytes: ByteArray?,
    isActivating: Boolean,
    onEditClick: () -> Unit,
    onListingClick: (() -> Unit)?,
    onActivateRideClick: () -> Unit,
) {
    val canActivateRide = vehicle.hasApprovedRideService() && !vehicle.isActiveRideVehicle
    val hasRideService = vehicle.hasRideService()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                VehicleThumbnail(photoBytes = vehiclePhotoBytes)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = vehicle.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = vehicle.plate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(30.dp),
                        ) {
                            Icon(
                                imageVector = Heroicons.Outline.EllipsisVertical,
                                contentDescription = "Vehicle actions",
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        vehicle.primaryServiceLabel()?.let { label ->
                            VehicleStatusPill(
                                label = label,
                                color = serviceColor(label),
                            )
                        }
                        VehicleStatusPill(
                            label = if (vehicle.isActiveRideVehicle) "Active" else vehicle.verificationStatus.statusLabel(),
                            color = if (vehicle.isActiveRideVehicle) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                vehicle.verificationStatus.statusColor()
                            },
                        )
                    }
                    Text(
                        text = vehicle.vehicleMetaLine(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = vehicle.serviceActionLabel(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (hasRideService) {
                            CityRideActiveSwitch(
                                checked = vehicle.isActiveRideVehicle,
                                enabled = canActivateRide && !isActivating,
                                onCheckedChange = {
                                    if (it && canActivateRide) {
                                        onActivateRideClick()
                                    }
                                },
                            )
                        }
                    }
                }
            }

            if (vehicle.verificationStatus == DriverRequirementStatus.Rejected ||
                vehicle.verificationStatus == DriverRequirementStatus.Missing
            ) {
                Text(
                    text = if (vehicle.verificationStatus == DriverRequirementStatus.Missing) {
                        "Vehicle documents are required."
                    } else {
                        "Vehicle documents need an update."
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VehicleCardAction(
                    icon = Heroicons.Outline.PencilSquare,
                    label = "Edit",
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                )
                if (onListingClick != null) {
                    VehicleCardAction(
                        icon = Heroicons.Outline.Tag,
                        label = "Listing",
                        onClick = onListingClick,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                    )
                }
                VehicleCardAction(
                    icon = if (vehicle.verificationStatus == DriverRequirementStatus.Missing ||
                        vehicle.verificationStatus == DriverRequirementStatus.Rejected
                    ) {
                        Heroicons.Outline.ArrowUpTray
                    } else {
                        Heroicons.Outline.DocumentText
                    },
                    label = if (vehicle.verificationStatus == DriverRequirementStatus.Missing ||
                        vehicle.verificationStatus == DriverRequirementStatus.Rejected
                    ) {
                        "Upload docs"
                    } else {
                        "Documents"
                    },
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CityRideActiveSwitch(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor = when {
        checked -> MaterialTheme.colorScheme.primary
        enabled -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
    }
    val thumbColor = when {
        checked -> MaterialTheme.colorScheme.onPrimary
        enabled -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    }
    val borderColor = if (checked) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 20.dp)
            .clip(CircleShape)
            .background(trackColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(thumbColor, CircleShape),
        )
    }
}

@Composable
private fun VehicleThumbnail(photoBytes: ByteArray?) {
    val shape = RoundedCornerShape(10.dp)

    Surface(
        modifier = Modifier.size(width = 82.dp, height = 68.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        if (photoBytes != null) {
            CapturedDocumentPreviewImage(
                bytes = photoBytes,
                contentDescription = "Vehicle photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.Truck,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun VehicleCardAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
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
    filter: VehicleServiceFilter = VehicleServiceFilter.All,
    onAddVehicleClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (filter == VehicleServiceFilter.All) "No vehicles yet" else "No ${filter.label.lowercase()} vehicles",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = if (filter == VehicleServiceFilter.All) {
                "Add a vehicle to prepare City Ride, rental, or cargo services."
            } else {
                "Vehicles for this service will appear here."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (filter == VehicleServiceFilter.All) {
            TextButton(onClick = onAddVehicleClick) {
                Text("Add vehicle")
            }
        }
    }
}

private enum class VehicleServiceFilter(
    val label: String,
    val serviceType: DriverVehicleServiceType?,
) {
    All("All", null),
    CityRide("City Rides", DriverVehicleServiceType.Ride),
    Rental("Rental", DriverVehicleServiceType.Rental),
    Cargo("Cargo", DriverVehicleServiceType.Cargo),
}

private fun List<DriverVehicle>.filterBy(filter: VehicleServiceFilter): List<DriverVehicle> {
    val serviceType = filter.serviceType ?: return this

    return filter { vehicle ->
        vehicle.services.any { it.serviceType == serviceType && it.isEnabled }
    }
}

private fun DriverVehicle.primaryServiceLabel(): String? {
    return services
        .filter { it.isEnabled }
        .sortedBy {
            when (it.serviceType) {
                DriverVehicleServiceType.Ride -> 0
                DriverVehicleServiceType.Rental -> 1
                DriverVehicleServiceType.Cargo -> 2
            }
        }
        .firstOrNull()
        ?.serviceType
        ?.displayName
}

private fun DriverVehicle.vehicleMetaLine(): String {
    val typeLabel = vehicleTypeCode
        ?.replace('_', ' ')
        ?.uppercaseFirstLetterOfEachWord()
    val capacityLabel = when {
        payloadKg != null && payloadKg > 0 -> "${payloadKg.cleanNumber()} kg"
        passengerCapacity != null && passengerCapacity > 0 -> "$passengerCapacity ${if (passengerCapacity == 1) "passenger" else "passengers"}"
        else -> null
    }

    return listOfNotNull(typeLabel, capacityLabel).joinToString(" • ").ifBlank { "Vehicle details" }
}

private fun DriverVehicle.serviceActionLabel(): String {
    return when {
        services.any { it.serviceType == DriverVehicleServiceType.Ride && it.isEnabled } -> {
            if (isActiveRideVehicle) "Currently used for Driver Mode" else "Available for city rides"
        }
        services.any { it.serviceType == DriverVehicleServiceType.Rental && it.isEnabled } -> "Available for rental bookings"
        services.any { it.serviceType == DriverVehicleServiceType.Cargo && it.isEnabled } -> "Available for cargo requests"
        else -> "No service selected"
    }
}

private fun DriverVehicle.hasRideService(): Boolean {
    return services.any { it.serviceType == DriverVehicleServiceType.Ride && it.isEnabled }
}

private fun DriverVehicle.listingServiceType(preferredServiceType: DriverVehicleServiceType?): DriverVehicleServiceType? {
    val listingServices = services
        .filter { it.isEnabled }
        .map { it.serviceType }
        .filter { it == DriverVehicleServiceType.Rental || it == DriverVehicleServiceType.Cargo }

    return if (preferredServiceType in listingServices) {
        preferredServiceType
    } else {
        listingServices.firstOrNull()
    }
}

@Composable
private fun serviceColor(label: String): Color {
    return when (label) {
        DriverVehicleServiceType.Ride.displayName -> MaterialTheme.colorScheme.primary
        DriverVehicleServiceType.Rental.displayName -> Color(0xFF7C3AED)
        DriverVehicleServiceType.Cargo.displayName -> Color(0xFFEA580C)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun Double.cleanNumber(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
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
