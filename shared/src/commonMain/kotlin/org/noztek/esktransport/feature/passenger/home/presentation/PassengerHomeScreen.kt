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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.composables.icons.heroicons.outline.BuildingStorefront
import asktransport_cmp.shared.generated.resources.Res
import asktransport_cmp.shared.generated.resources.home_big_truck
import asktransport_cmp.shared.generated.resources.home_car
import asktransport_cmp.shared.generated.resources.home_scooter
import asktransport_cmp.shared.generated.resources.home_tricycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.noztek.esktransport.core.platform.isIosPlatform

@Composable
fun PassengerHomeScreen(
    onWhereToClick: () -> Unit = {},
    onSuggestionClick: (Int) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val topPadding = if (isIosPlatform()) 4.dp else 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
            .padding(top = topPadding, bottom = 12.dp),
    ) {
        SearchRow(onWhereToClick = onWhereToClick)
        Spacer(modifier = Modifier.height(10.dp))
        PlaceSuggestionsRow()
        Spacer(modifier = Modifier.height(14.dp))

        AddressCard(title = "Command Center", subtitle = "Kenram, Isulan", icon = Heroicons.Outline.Clock)
        Spacer(modifier = Modifier.height(8.dp))
        AddressCard(title = "Home", subtitle = "Mabini Street, Poblacion, Tacurong City", icon = Heroicons.Outline.MapPin)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("See all", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(suggestions) { item ->
                SuggestionCard(item = item, onClick = { onSuggestionClick(item.vehicleTypeIndex) })
            }
        }
    }
}

@Composable
private fun PlaceSuggestionsRow() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rowItems(placeSuggestions) { place ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF6F7F9),
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(place.icon, contentDescription = place.name, modifier = Modifier.size(16.dp))
                    Text(place.name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SearchRow(onWhereToClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFFF3F4F6),
        modifier = Modifier.fillMaxWidth(),
        onClick = onWhereToClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Heroicons.Outline.MagnifyingGlass, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Where to?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(26.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.width(10.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Heroicons.Outline.CalendarDays, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Later", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun AddressCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, Color(0xFFE7E9EF)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(color = Color(0xFFF4F6FA), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
                }
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SuggestionCard(item: SuggestionItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA)),
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
            Text(item.label, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private data class SuggestionItem(
    val label: String,
    val image: DrawableResource,
    val vehicleTypeIndex: Int,
)
private data class PlaceSuggestion(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val suggestions = listOf(
    SuggestionItem("Moto", Res.drawable.home_scooter, vehicleTypeIndex = 0),
    SuggestionItem("Trike", Res.drawable.home_tricycle, vehicleTypeIndex = 1),
    SuggestionItem("Car", Res.drawable.home_car, vehicleTypeIndex = 2),
    SuggestionItem("Rentals", Res.drawable.home_big_truck, vehicleTypeIndex = 3),
)

private val placeSuggestions = listOf(
    PlaceSuggestion("SM Mall", Heroicons.Outline.BuildingStorefront),
    PlaceSuggestion("State University", Heroicons.Outline.AcademicCap),
    PlaceSuggestion("St. Louis", Heroicons.Outline.BuildingLibrary),
    PlaceSuggestion("Public Market", Heroicons.Outline.BuildingStorefront),
    PlaceSuggestion("Airport", Heroicons.Outline.PaperAirplane),
    PlaceSuggestion("Church", Heroicons.Outline.BookOpen),
)
