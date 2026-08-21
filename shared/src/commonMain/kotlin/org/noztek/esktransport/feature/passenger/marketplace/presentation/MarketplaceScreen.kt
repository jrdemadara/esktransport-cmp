package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.outline.User
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
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBackClick: () -> Unit = {},
    onListingClick: (String) -> Unit = {},
    viewModel: MarketplaceViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MarketplaceTopBar(
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MarketplaceSearchField()
            }
            item {
                MarketplaceSectionTitle(title = "Vehicle types")
            }
            when {
                uiState.isLoadingVehicleTypes && uiState.vehicleTypes.isEmpty() -> {
                    item {
                        MarketplaceLoadingRow(text = "Loading vehicle types")
                    }
                }
                uiState.vehicleTypes.isNotEmpty() -> {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                VehicleTypeFilterChip(
                                    label = "all",
                                    image = Res.drawable.home_big_truck,
                                    selected = uiState.selectedVehicleTypeCode == null,
                                    onClick = { viewModel.selectVehicleType(null) },
                                )
                            }
                            items(uiState.vehicleTypes) { item ->
                                VehicleTypeFilterChip(
                                    label = item.name.lowercase(),
                                    image = vehicleTypeImage(item.code),
                                    selected = uiState.selectedVehicleTypeCode == item.code,
                                    onClick = { viewModel.selectVehicleType(item.code) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                MarketplaceSectionTitle(title = "Available rentals")
            }
            when {
                uiState.isLoadingListings && uiState.listings.isEmpty() -> {
                    item {
                        MarketplaceLoadingRow(text = "Loading rentals")
                    }
                }
                uiState.errorMessage != null && uiState.listings.isEmpty() -> {
                    item {
                        MarketplaceStateMessage(
                            title = "Unable to load rentals",
                            message = uiState.errorMessage.orEmpty(),
                            actionText = "Retry",
                            onActionClick = viewModel::refresh,
                        )
                    }
                }
                uiState.listings.isEmpty() -> {
                    item {
                        MarketplaceStateMessage(
                            title = "No rentals yet",
                            message = "Try another vehicle type when more owners publish listings.",
                        )
                    }
                }
                else -> {
                    items(uiState.listings) { listing ->
                        MarketplaceListingCard(
                            listing = listing,
                            vehiclePhotoBytes = uiState.listingPhotoBytes[listing.publicId],
                            onClick = { onListingClick(listing.publicId) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceTopBar(
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
                text = "Marketplace",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        },
    )
}

@Composable
private fun MarketplaceSearchField() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = "Search vehicles",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Heroicons.Outline.MagnifyingGlass,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun MarketplaceSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun VehicleTypeFilterChip(
    label: String,
    image: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(width = 78.dp, height = 74.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            width = 0.8.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MarketplaceLoadingRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MarketplaceStateMessage(
    title: String,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionText != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun MarketplaceListingCard(
    listing: MarketplaceListing,
    vehiclePhotoBytes: ByteArray?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ListingImage(
                    listing = listing,
                    vehiclePhotoBytes = vehiclePhotoBytes,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = listing.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Heroicons.Outline.Heart,
                            contentDescription = "Save",
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OwnerLine(name = listing.owner.name)
                    SmallBluePill(text = listing.vehicle.vehicleTypeCode)
                    Text(
                        text = listing.vehicle.modelYearLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listing.vehicle.plate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            ListingStatsRow(listing = listing)
            MarketplaceDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(text = "active", color = Color(0xFF16A34A))
                    StatusPill(text = "approved", color = Color(0xFFD97706))
                }
                AvailablePill()
            }
        }
    }
}

@Composable
private fun ListingImage(
    listing: MarketplaceListing,
    vehiclePhotoBytes: ByteArray?,
) {
    Box(
        modifier = Modifier
            .size(width = 122.dp, height = 112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    ) {
        vehiclePhotoBytes?.let { bytes ->
            CapturedDocumentPreviewImage(
                bytes = bytes,
                contentDescription = listing.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                text = listing.vehicle.vehicleTypeCode,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OwnerLine(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Heroicons.Outline.User,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Heroicons.Outline.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SmallBluePill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ListingStatsRow(listing: MarketplaceListing) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ListingStat(value = formatWeight(listing.vehicle.payloadKg), label = "Payload", modifier = Modifier.weight(1f))
        ThinDivider()
        ListingStat(value = listing.vehicle.passengerCapacity?.toString() ?: "-", label = "Passengers", modifier = Modifier.weight(1f))
        ThinDivider()
        ListingStat(value = listing.rateLabel(), label = "Base rate", modifier = Modifier.weight(1.28f))
        ThinDivider()
        ListingStat(value = listing.includedKmLabel(), label = "included km", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ListingStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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

@Composable
private fun ThinDivider() {
    Box(
        modifier = Modifier
            .width(0.7.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun MarketplaceDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.45f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun AvailablePill() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Available",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun vehicleTypeImage(code: String): DrawableResource {
    return when (code) {
        "motorcycle" -> Res.drawable.home_scooter
        "tricycle" -> Res.drawable.home_tricycle
        "sedan", "hatchback", "car", "suv", "mpv" -> Res.drawable.home_car
        "pickup", "multicab" -> Res.drawable.car
        "van", "jeepney" -> Res.drawable.big_truck
        "mini_truck", "light_truck", "cargo_truck", "closed_van", "wing_van", "fleet_vehicle" -> Res.drawable.medium_truck
        else -> Res.drawable.home_big_truck
    }
}

private fun MarketplaceListing.rateLabel(): String {
    val amount = baseRate ?: return "-"
    val unit = rateUnit?.takeIf { it.isNotBlank() } ?: "day"
    return "${formatMoney(amount, currency)}/$unit"
}

private fun MarketplaceListing.includedKmLabel(): String {
    return includedKm?.let { "${formatCompactNumber(it)} km" } ?: "-"
}

private fun org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListingVehicle.modelYearLabel(): String {
    val name = listOfNotNull(make, model).joinToString(" ").ifBlank {
        vehicleTypeLabel ?: vehicleTypeCode
    }
    return year?.let { "$name  •  $it" } ?: name
}

private fun formatWeight(value: Double?): String {
    return value?.let { "${formatCompactNumber(it)} kg" } ?: "-"
}

private fun formatMoney(amount: Double, currency: String): String {
    val whole = amount.toLong()
    val formatted = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (currency.equals("PHP", ignoreCase = true)) {
        "PHP $formatted"
    } else {
        "$currency $formatted"
    }
}

private fun formatCompactNumber(value: Double): String {
    val whole = value.toLong()
    return if (value == whole.toDouble()) {
        whole.toString().reversed().chunked(3).joinToString(",").reversed()
    } else {
        value.toString()
    }
}
