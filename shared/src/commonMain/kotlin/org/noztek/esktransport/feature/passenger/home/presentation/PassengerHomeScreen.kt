package org.noztek.esktransport.feature.passenger.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.outline.BookOpen
import com.composables.icons.heroicons.outline.BuildingStorefront
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.AcademicCap
import com.composables.icons.heroicons.outline.BuildingLibrary
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.PaperAirplane
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.outline.Truck
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.home_big_truck
import esktransport.shared.generated.resources.home_car
import esktransport.shared.generated.resources.home_scooter
import esktransport.shared.generated.resources.home_tricycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.feature.passenger.home.domain.model.KnownPlace
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlaceType

@Composable
fun PassengerHomeScreen(
    onWhereToClick: () -> Unit = {},
    onPlaceClick: (label: String, point: GeoPoint?) -> Unit = { _, _ -> },
    onSuggestionClick: (Int) -> Unit = {},
    onMarketplaceClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: PassengerHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 12.dp),
    ) {
        SearchRow(onWhereToClick = onWhereToClick)
        Spacer(modifier = Modifier.height(10.dp))
        PlaceSuggestionsRow(
            places = uiState.knownPlaces,
            isLoading = uiState.isLoading,
            onPlaceClick = onPlaceClick,
        )
        Spacer(modifier = Modifier.height(14.dp))

        SavedPlacesSection(
            places = uiState.savedPlaces,
            isLoading = uiState.isLoading,
            onPlaceClick = onPlaceClick,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Choose your ride",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "See all",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            suggestions.forEach { item ->
                SuggestionCard(
                    item = item,
                    onClick = {
                        if (item.opensMarketplace) {
                            onMarketplaceClick()
                        } else {
                            onSuggestionClick(item.vehicleTypeIndex)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        RentalVehicleCard(onClick = onMarketplaceClick)
    }
}

@Composable
private fun PlaceSuggestionsRow(
    places: List<KnownPlace>,
    isLoading: Boolean,
    onPlaceClick: (label: String, point: GeoPoint?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isLoading && places.isEmpty()) {
            rowItems(listOf("Loading places")) { label ->
                PlaceSuggestionChip(name = label, icon = Heroicons.Outline.MapPin, enabled = false)
            }
        } else {
            rowItems(places) { place ->
                PlaceSuggestionChip(
                    name = place.name,
                    icon = place.category.knownPlaceIcon(),
                    enabled = place.latitude != null && place.longitude != null,
                    onClick = {
                        onPlaceClick(
                            place.name,
                            place.toGeoPoint(),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun PlaceSuggestionChip(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                icon,
                contentDescription = name,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SavedPlacesSection(
    places: List<SavedPlace>,
    isLoading: Boolean,
    onPlaceClick: (label: String, point: GeoPoint?) -> Unit,
) {
    val visiblePlaces = places.take(2)
    when {
        isLoading && places.isEmpty() -> {
            AddressCard(title = "Loading saved places", subtitle = "Checking your saved addresses", icon = Heroicons.Outline.Clock)
        }
        visiblePlaces.isEmpty() -> {
            AddressCard(
                title = "Save your places",
                subtitle = "Add Home or Work from Settings for faster booking.",
                icon = Heroicons.Outline.MapPin,
            )
        }
        else -> {
            visiblePlaces.forEachIndexed { index, place ->
                AddressCard(
                    title = place.label,
                    subtitle = place.address,
                    icon = place.placeType.savedPlaceIcon(),
                    onClick = {
                        onPlaceClick(place.label, place.toGeoPoint())
                    },
                )
                if (index < visiblePlaces.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AddressCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            AddressCardContent(title = title, subtitle = subtitle, icon = icon)
        }
    } else {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            AddressCardContent(title = title, subtitle = subtitle, icon = icon)
        }
    }
}

@Composable
private fun AddressCardContent(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchRow(onWhereToClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        onClick = onWhereToClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Heroicons.Outline.MagnifyingGlass,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Where to?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(modifier = Modifier.height(26.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Heroicons.Outline.CalendarDays,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Later",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RentalVehicleCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.Truck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rent a vehicle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Long distance, family trips or bulk loads.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rentalVehicles.forEach { item ->
                    RentalVehicleOption(item = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RentalVehicleOption(item: RentalVehicleItem, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(item.image),
                contentDescription = item.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(38.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SuggestionCard(item: SuggestionItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 86.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(item.image),
                contentDescription = item.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(42.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class SuggestionItem(
    val label: String,
    val image: DrawableResource,
    val vehicleTypeIndex: Int,
    val opensMarketplace: Boolean = false,
)
private data class RentalVehicleItem(val label: String, val image: DrawableResource)

private fun SavedPlace.toGeoPoint(): GeoPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng)
}

private fun KnownPlace.toGeoPoint(): GeoPoint? {
    val lat = latitude ?: return null
    val lng = longitude ?: return null
    return GeoPoint(lat, lng)
}

private fun SavedPlaceType.savedPlaceIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        SavedPlaceType.Home -> Heroicons.Outline.MapPin
        SavedPlaceType.Work -> Heroicons.Outline.BuildingStorefront
        SavedPlaceType.Custom -> Heroicons.Outline.Clock
    }
}

private fun String.knownPlaceIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (lowercase()) {
        "mall", "market" -> Heroicons.Outline.BuildingStorefront
        "school", "university" -> Heroicons.Outline.AcademicCap
        "government" -> Heroicons.Outline.BuildingLibrary
        "airport", "terminal" -> Heroicons.Outline.PaperAirplane
        "church" -> Heroicons.Outline.BookOpen
        else -> Heroicons.Outline.MapPin
    }
}

private val suggestions = listOf(
    SuggestionItem("Moto", Res.drawable.home_scooter, vehicleTypeIndex = 0),
    SuggestionItem("Trike", Res.drawable.home_tricycle, vehicleTypeIndex = 1),
    SuggestionItem("Car", Res.drawable.home_car, vehicleTypeIndex = 2),
    SuggestionItem("Rentals", Res.drawable.home_big_truck, vehicleTypeIndex = 3, opensMarketplace = true),
)

private val rentalVehicles = listOf(
    RentalVehicleItem("Van", Res.drawable.home_car),
    RentalVehicleItem("Truck", Res.drawable.home_big_truck),
    RentalVehicleItem("Pickup", Res.drawable.home_big_truck),
)
