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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onBackClick: () -> Unit = {},
) {
    var selectedVehicleType by remember { mutableStateOf("van") }

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
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vehicleTypeFilters) { item ->
                        VehicleTypeFilterChip(
                            item = item,
                            selected = selectedVehicleType == item.key,
                            onClick = { selectedVehicleType = item.key },
                        )
                    }
                }
            }
            item {
                MarketplaceSectionTitle(title = "Available rentals")
            }
            items(mockListings.filter { it.service == MarketplaceService.Rental }) { listing ->
                MarketplaceListingCard(listing = listing)
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
    item: VehicleTypeFilter,
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
                painter = painterResource(item.image),
                contentDescription = item.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
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
private fun MarketplaceListingCard(listing: MarketplaceListing) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ListingImage(listing = listing)
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
                            style = MaterialTheme.typography.titleMedium,
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
                    OwnerLine(name = listing.owner)
                    SmallBluePill(text = listing.typeLabel)
                    Text(
                        text = "${listing.makeModel}  •  ${listing.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listing.plate,
                        style = MaterialTheme.typography.bodyMedium,
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
private fun ListingImage(listing: MarketplaceListing) {
    Box(
        modifier = Modifier
            .size(width = 122.dp, height = 112.dp)
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Image(
            painter = painterResource(listing.image),
            contentDescription = listing.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text(
                text = listing.typeLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelLarge,
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
            style = MaterialTheme.typography.bodyMedium,
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
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
        ListingStat(value = listing.payload, label = "Payload", modifier = Modifier.weight(1f))
        ThinDivider()
        ListingStat(value = listing.passengers, label = "Passengers", modifier = Modifier.weight(1f))
        ThinDivider()
        ListingStat(value = listing.rate, label = "Base rate", modifier = Modifier.weight(1.28f))
        ThinDivider()
        ListingStat(value = listing.includedKm, label = "included km", modifier = Modifier.weight(1f))
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
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
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private enum class MarketplaceService {
    Rental,
    Cargo,
}

private data class VehicleTypeFilter(
    val key: String,
    val label: String,
    val image: DrawableResource,
)

private data class MarketplaceListing(
    val service: MarketplaceService,
    val typeLabel: String,
    val title: String,
    val owner: String,
    val makeModel: String,
    val year: String,
    val plate: String,
    val payload: String,
    val passengers: String,
    val rate: String,
    val includedKm: String,
    val image: DrawableResource,
)

private val vehicleTypeFilters = listOf(
    VehicleTypeFilter("van", "van", Res.drawable.home_big_truck),
    VehicleTypeFilter("pickup", "pickup", Res.drawable.home_car),
    VehicleTypeFilter("truck", "truck", Res.drawable.home_big_truck),
    VehicleTypeFilter("sedan", "sedan", Res.drawable.home_car),
    VehicleTypeFilter("motorcycle", "motorcycle", Res.drawable.home_scooter),
    VehicleTypeFilter("tricycle", "tricycle", Res.drawable.home_tricycle),
)

private val mockListings = listOf(
    MarketplaceListing(
        service = MarketplaceService.Rental,
        typeLabel = "van",
        title = "Isuzu N-Series Box Truck",
        owner = "Juan Dela Cruz",
        makeModel = "Isuzu N-Series",
        year = "2021",
        plate = "ABC 1234",
        payload = "4,200 kg",
        passengers = "0",
        rate = "PHP 6,800/day",
        includedKm = "100 km",
        image = Res.drawable.medium_truck,
    ),
    MarketplaceListing(
        service = MarketplaceService.Rental,
        typeLabel = "pickup",
        title = "Toyota Hilux 4x4",
        owner = "Pedro Santos",
        makeModel = "Toyota Hilux 4x4",
        year = "2020",
        plate = "DEF 5678",
        payload = "1,000 kg",
        passengers = "5",
        rate = "PHP 2,500/day",
        includedKm = "80 km",
        image = Res.drawable.car,
    ),
    MarketplaceListing(
        service = MarketplaceService.Rental,
        typeLabel = "van",
        title = "Nissan Urvan NV350",
        owner = "Mark Reyes",
        makeModel = "Nissan Urvan NV350",
        year = "2019",
        plate = "GHI 9012",
        payload = "1,200 kg",
        passengers = "12",
        rate = "PHP 3,200/day",
        includedKm = "100 km",
        image = Res.drawable.big_truck,
    ),
    MarketplaceListing(
        service = MarketplaceService.Cargo,
        typeLabel = "truck",
        title = "Isuzu Elf Cargo",
        owner = "Roberto Manuel",
        makeModel = "Isuzu Elf",
        year = "2022",
        plate = "CRG 7712",
        payload = "3,500 kg",
        passengers = "2",
        rate = "PHP 5,900/trip",
        includedKm = "60 km",
        image = Res.drawable.medium_truck,
    ),
    MarketplaceListing(
        service = MarketplaceService.Cargo,
        typeLabel = "pickup",
        title = "Pickup with Canopy",
        owner = "Ana Lopez",
        makeModel = "Toyota Hilux",
        year = "2021",
        plate = "PKP 8820",
        payload = "900 kg",
        passengers = "4",
        rate = "PHP 2,800/trip",
        includedKm = "50 km",
        image = Res.drawable.car,
    ),
)
