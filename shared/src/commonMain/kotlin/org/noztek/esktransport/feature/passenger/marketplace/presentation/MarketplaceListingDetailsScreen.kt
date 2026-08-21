package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Briefcase
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Cube
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Tag
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingDetailsScreen(
    listingPublicId: String,
    onBackClick: () -> Unit = {},
    viewModel: MarketplaceListingDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(listingPublicId) {
        viewModel.load(listingPublicId)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListingDetailsTopBar(onBackClick = onBackClick)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val listing = uiState.listing
            when {
                uiState.isLoading && listing == null -> {
                    item { DetailsLoadingState() }
                }
                uiState.errorMessage != null && listing == null -> {
                    item {
                        DetailsMessageState(
                            title = "Unable to load listing",
                            message = uiState.errorMessage.orEmpty(),
                        )
                    }
                }
                listing != null -> {
                    item {
                        ListingImageCarousel(
                            listing = listing,
                            vehiclePhotoBytes = uiState.vehiclePhotoBytes,
                        )
                    }
                    item { ListingHeader(listing = listing) }
                    item { VehicleDetailsCard(listing = listing) }
                    item { PricingCard(listing = listing) }
                    item { DescriptionCard(listing = listing) }
                    item { RequestVehicleCard() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingDetailsTopBar(
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
                text = "Listing details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        },
    )
}

@Composable
private fun ListingImageCarousel(
    listing: MarketplaceListing,
    vehiclePhotoBytes: ByteArray?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        ) {
            vehiclePhotoBytes?.let { bytes ->
                CapturedDocumentPreviewImage(
                    bytes = bytes,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(7.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    contentColor = Color.White,
                ) {
                    Text(
                        text = "1 / 1",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListingHeader(listing: MarketplaceListing) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SolidPill(text = listing.serviceType.replaceFirstChar { it.uppercase() })
                    SoftPill(text = listing.vehicle.vehicleTypeCode, color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusPill(text = "active", color = Color(0xFF16A34A))
                    StatusPill(text = "approved", color = Color(0xFFD97706))
                }
            }
            Icon(
                imageVector = Heroicons.Outline.Heart,
                contentDescription = "Save",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
        OwnerRow(listing = listing)
    }
}

@Composable
private fun OwnerRow(listing: MarketplaceListing) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.User,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = listing.owner.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Icon(
                imageVector = Heroicons.Outline.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = listing.updatedAt?.let { "Updated ${formatApiDate(it)}" } ?: "Recently updated",
            modifier = Modifier.padding(start = 22.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleDetailsCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Vehicle details",
        icon = Heroicons.Outline.Truck,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.DocumentText,
                label = "Plate number",
                value = listing.vehicle.plate,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Truck,
                label = "Make / Model / Year",
                value = listing.vehicle.modelYearLabel(),
                modifier = Modifier.weight(1.2f),
            )
        }
        SectionDivider()
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.Briefcase,
                label = "Payload",
                value = formatWeight(listing.vehicle.payloadKg),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Cube,
                label = "Volume",
                value = listing.vehicle.volumeM3?.let { "${formatCompactNumber(it)} m3" } ?: "-",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.User,
                label = "Passenger capacity",
                value = listing.vehicle.passengerCapacity?.toString() ?: "-",
                modifier = Modifier.weight(1.08f),
            )
        }
        SectionDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Availability",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Available",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PricingCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Pricing",
        icon = Heroicons.Outline.Tag,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Base rate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = listing.rateLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = listing.minimumHours?.let { "Minimum ${formatCompactNumber(it)} hours" } ?: "Minimum hours not set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalDivider(modifier = Modifier.height(58.dp))
            Column(
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Included km",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = listing.includedKm?.let { "${formatCompactNumber(it)} km" } ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            SoftPill(text = listing.currency, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DescriptionCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Description",
        icon = Heroicons.Outline.DocumentText,
    ) {
        Text(
            text = listing.description?.takeIf { it.isNotBlank() } ?: "No description provided yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestVehicleCard() {
    DetailsSectionCard(
        title = "Request this vehicle",
        icon = Heroicons.Outline.CalendarDays,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestField(
                label = "Requested start",
                icon = Heroicons.Outline.CalendarDays,
                modifier = Modifier.weight(1f),
            )
            RequestField(
                label = "Requested end",
                icon = Heroicons.Outline.CalendarDays,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestField(
                label = "Pickup location",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
            RequestField(
                label = "Destination",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
        }
        RequestField(
            label = "Customer note (optional)",
            icon = Heroicons.Outline.DocumentText,
            showChevron = false,
        )
        AppPrimaryButton(
            text = "Send request",
            onClick = {},
            height = 44.dp,
        )
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.ChatBubbleOvalLeft,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Message owner",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DetailsLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Loading listing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailsMessageState(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
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
        }
    }
}

@Composable
private fun DetailsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RequestField(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showChevron) {
                Icon(
                    imageVector = Heroicons.Outline.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SolidPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
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
private fun SoftPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
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
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

private fun MarketplaceListing.rateLabel(): String {
    val amount = baseRate ?: return "-"
    val unit = rateUnit?.takeIf { it.isNotBlank() } ?: "day"
    return "${formatMoney(amount, currency)}/$unit"
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

private fun formatApiDate(value: String): String {
    val date = value.take(10)
    val parts = date.split("-")
    if (parts.size != 3) return date

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> return date
    }
    val day = parts[2].trimStart('0').ifBlank { parts[2] }
    return "$month $day, ${parts[0]}"
}
