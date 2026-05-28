package org.noztek.esktransport.feature.passenger.home.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Car
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.GraduationCap
import com.composables.icons.lucide.Landmark
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Plane
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Store
import com.composables.icons.lucide.Zap

@Composable
fun PassengerHomeScreen(
    onWhereToClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        SearchRow(onWhereToClick = onWhereToClick)
        Spacer(modifier = Modifier.height(10.dp))
        PlaceSuggestionsRow()
        Spacer(modifier = Modifier.height(14.dp))

        AddressCard(title = "Command Center", subtitle = "Kenram, Isulan", icon = Lucide.Clock)
        Spacer(modifier = Modifier.height(8.dp))
        AddressCard(title = "Home", subtitle = "Mabini Street, Poblacion, Tacurong City", icon = Lucide.MapPin)
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
                SuggestionCard(item = item, onClick = onWhereToClick)
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
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp,
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
        color = lerp(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface, 0.35f),
        modifier = Modifier.fillMaxWidth(),
        onClick = onWhereToClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Lucide.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Where to?", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(26.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.width(10.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Lucide.CalendarDays, contentDescription = null, modifier = Modifier.size(18.dp))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(34.dp)) {
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.label, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private data class SuggestionItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private data class PlaceSuggestion(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val suggestions = listOf(
    SuggestionItem("Moto", Lucide.Zap),
    SuggestionItem("Trike", Lucide.MapPin),
    SuggestionItem("Car", Lucide.Car),
    SuggestionItem("Rentals", Lucide.Clock),
)

private val placeSuggestions = listOf(
    PlaceSuggestion("SM Mall", Lucide.Store),
    PlaceSuggestion("State University", Lucide.GraduationCap),
    PlaceSuggestion("St. Louis", Lucide.Landmark),
    PlaceSuggestion("Public Market", Lucide.Building2),
    PlaceSuggestion("Airport", Lucide.Plane),
    PlaceSuggestion("Church", Lucide.BookOpen),
)
