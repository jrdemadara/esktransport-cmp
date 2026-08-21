package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.Eye
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.Tag
import com.composables.icons.heroicons.outline.Truck
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.big_truck
import esktransport.shared.generated.resources.car
import esktransport.shared.generated.resources.home_big_truck
import esktransport.shared.generated.resources.home_car
import esktransport.shared.generated.resources.home_scooter
import esktransport.shared.generated.resources.home_tricycle
import esktransport.shared.generated.resources.medium_truck
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverVehicleServiceType
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListing
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverMarketplaceListingVehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingEditScreen(
    vehiclePublicId: String?,
    serviceType: DriverVehicleServiceType,
    onBackClick: () -> Unit,
    viewModel: MarketplaceListingEditViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vehiclePublicId, serviceType) {
        viewModel.load(vehiclePublicId, serviceType)
    }

    LaunchedEffect(uiState.errorMessage, uiState.statusMessage) {
        val message = uiState.errorMessage ?: uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessages()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ListingEditTopBar(
                title = serviceType.screenTitle(),
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            ListingEditBottomActions(
                isSaving = uiState.isSaving,
                onSaveClick = viewModel::save,
                onPreviewClick = {},
            )
        },
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
            uiState.listing == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Listing not found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    VehicleListingSummaryCard(listing = uiState.listing!!)
                    ListingFormCard(
                        title = uiState.title,
                        onTitleChange = viewModel::onTitleChange,
                        description = uiState.description,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        baseRate = uiState.baseRate,
                        onBaseRateChange = viewModel::onBaseRateChange,
                        rateUnit = uiState.rateUnit,
                        onRateUnitChange = viewModel::onRateUnitChange,
                        minimumHours = uiState.minimumHours,
                        onMinimumHoursChange = viewModel::onMinimumHoursChange,
                        includedKm = uiState.includedKm,
                        onIncludedKmChange = viewModel::onIncludedKmChange,
                        isAvailable = uiState.isAvailable,
                        onAvailableChange = viewModel::onAvailableChange,
                    )
                    PublishingCard(
                        listing = uiState.listing!!,
                        isSaving = uiState.isSaving,
                        onPublishClick = viewModel::publish,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingEditTopBar(
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
                maxLines = 1,
            )
        },
    )
}

@Composable
private fun VehicleListingSummaryCard(
    listing: DriverMarketplaceListing,
) {
    val vehicle = listing.vehicle
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(vehicle.vehicleTypeImage()),
                contentDescription = vehicle.displayName(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 112.dp, height = 86.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = vehicle.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconText(
                    icon = Heroicons.Outline.Tag,
                    text = vehicle.plate,
                )
                IconText(
                    icon = Heroicons.Outline.Truck,
                    text = vehicle.vehicleTypeLabel ?: vehicle.vehicleTypeCode.orEmpty().titleLabel(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallStatusPill(
                        text = listing.serviceStatus.statusLabel(),
                        color = Color(0xFF16A34A),
                        icon = Heroicons.Outline.CheckCircle,
                    )
                    SmallStatusPill(
                        text = listing.status.titleLabel(),
                        color = listing.status.statusColor(),
                        icon = if (listing.status == "active") Heroicons.Outline.CheckCircle else Heroicons.Outline.Clock,
                    )
                }
                vehicle.publicId?.takeLast(6)?.let { shortId ->
                    Text(
                        text = "Ref $shortId",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconText(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SmallStatusPill(
    text: String,
    color: Color,
    icon: ImageVector,
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ListingFormCard(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    baseRate: String,
    onBaseRateChange: (String) -> Unit,
    rateUnit: String,
    onRateUnitChange: (String) -> Unit,
    minimumHours: String,
    onMinimumHoursChange: (String) -> Unit,
    includedKm: String,
    isAvailable: Boolean,
    onIncludedKmChange: (String) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompactInput(
                label = "Listing title",
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
            )
            CompactInput(
                label = "Description",
                value = description,
                onValueChange = onDescriptionChange,
                minLines = 3,
                maxLines = 4,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompactInput(
                    label = "Base rate",
                    value = baseRate,
                    onValueChange = onBaseRateChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardType = KeyboardType.Decimal,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FieldLabel("Rate unit")
                    RateUnitSelector(
                        selected = rateUnit,
                        onSelect = onRateUnitChange,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompactInput(
                    label = "Minimum hours",
                    value = minimumHours,
                    onValueChange = onMinimumHoursChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                )
                CompactInput(
                    label = "Included km",
                    value = includedKm,
                    onValueChange = onIncludedKmChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardType = KeyboardType.Number,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Heroicons.Outline.CalendarDays,
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
                        text = "Available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Show this vehicle for customer requests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(
                    checked = isAvailable,
                    onCheckedChange = onAvailableChange,
                )
            }
        }
    }
}

@Composable
private fun CompactInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(8.dp),
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun RateUnitSelector(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val units = listOf("hour", "day", "trip")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp)),
    ) {
        units.forEachIndexed { index, unit ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        if (unit == selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                    )
                    .clickable { onSelect(unit) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (unit == selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            if (index < units.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}

@Composable
private fun PublishingCard(
    listing: DriverMarketplaceListing,
    isSaving: Boolean,
    onPublishClick: () -> Unit,
) {
    val canPublish = listing.serviceStatus == DriverRequirementStatus.Approved &&
        listing.serviceEnabled &&
        listing.status != "active" &&
        !isSaving

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Heroicons.Outline.PaperAirplane,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Publishing",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (listing.status == "active") {
                        "This listing is visible to customers."
                    } else {
                        "Publish this ${listing.serviceType.shortListingLabel()} listing when pricing and details are ready."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = onPublishClick,
                    enabled = canPublish,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.PaperAirplane,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSaving) "Saving..." else if (listing.status == "active") "Published" else "Publish listing")
                }
            }
        }
    }
}

@Composable
private fun ListingEditBottomActions(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onPreviewClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppPrimaryButton(
                text = if (isSaving) "Saving..." else "Save changes",
                onClick = onSaveClick,
                enabled = !isSaving,
                height = 46.dp,
                trailingIcon = {
                    Icon(
                        imageVector = Heroicons.Outline.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                },
            )
            OutlinedButton(
                onClick = onPreviewClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(vertical = 11.dp),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Eye,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preview listing",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun DriverVehicleServiceType.screenTitle(): String {
    return when (this) {
        DriverVehicleServiceType.Cargo -> "Cargo listing"
        DriverVehicleServiceType.Rental,
        DriverVehicleServiceType.Ride -> "Rental listing"
    }
}

private fun DriverVehicleServiceType.shortListingLabel(): String {
    return when (this) {
        DriverVehicleServiceType.Ride -> "city ride"
        DriverVehicleServiceType.Rental -> "rental"
        DriverVehicleServiceType.Cargo -> "cargo"
    }
}

private fun DriverMarketplaceListingVehicle.displayName(): String {
    return listOfNotNull(make, model)
        .joinToString(" ")
        .ifBlank { vehicleTypeLabel ?: vehicleTypeCode ?: "Vehicle" }
        .replace('_', ' ')
        .uppercaseFirstLetterOfEachWord()
}

private fun DriverMarketplaceListingVehicle.vehicleTypeImage(): DrawableResource {
    return when (vehicleTypeCode?.lowercase()) {
        "motorcycle" -> Res.drawable.home_scooter
        "tricycle" -> Res.drawable.home_tricycle
        "sedan", "hatchback", "car", "suv", "mpv" -> Res.drawable.home_car
        "pickup", "multicab" -> Res.drawable.car
        "van", "jeepney" -> Res.drawable.big_truck
        "mini_truck", "light_truck", "cargo_truck", "closed_van", "wing_van", "fleet_vehicle" -> {
            Res.drawable.medium_truck
        }
        else -> Res.drawable.home_big_truck
    }
}

private fun String.titleLabel(): String {
    return split("_", "-")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.uppercaseFirstLetterOfEachWord() }
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
private fun String.statusColor(): Color {
    return when (this) {
        "active" -> Color(0xFF16A34A)
        "draft" -> Color(0xFFD97706)
        "paused" -> MaterialTheme.colorScheme.onSurfaceVariant
        "archived" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
