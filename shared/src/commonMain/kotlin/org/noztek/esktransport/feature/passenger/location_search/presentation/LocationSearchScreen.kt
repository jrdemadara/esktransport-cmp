package org.noztek.esktransport.feature.passenger.location_search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Search
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    mode: String,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onLocationSelected: (SelectedLocation) -> Unit = {},
    viewModel: LocationSearchViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val normalizedMode = if (mode == "pickup") "pickup" else "destination"
    val title = if (normalizedMode == "pickup") "Search pickup point" else "Search destination"
    val selectActionText = if (normalizedMode == "pickup") "Select as Pickup Point" else "Select as Destination"
    val selectedPoint = state.selectedPoint ?: state.currentLocationPoint ?: GeoPoint(6.6920431660391095, 124.68050838312321)
    val selectedLabel = state.tappedLocationLabel ?: "Move or search to pick a location"

    LaunchedEffect(normalizedMode) { viewModel.onScreenOpened() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Lucide.Search, contentDescription = null) },
                placeholder = { Text("Search location") },
                singleLine = true,
            )
            if (state.suggestions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    items(state.suggestions) { suggestion ->
                        Text(
                            text = suggestion.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSuggestionSelected(suggestion) }
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PlatformMapView(
                modifier = Modifier.fillMaxSize(),
                config = mapboxConfig,
                cameraCenter = MapPoint(selectedPoint.latitude, selectedPoint.longitude),
                cameraDefaults = MapCameraDefaults(zoom = 15.5, pitch = 30.0),
                markers = listOf(
                    MapMarker("selected", MapPoint(selectedPoint.latitude, selectedPoint.longitude), Color(0xFF2563EB), 8.0),
                ),
            )
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 6.dp,
            ) {
                Icon(
                    Lucide.MapPin,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(28.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(selectedLabel, style = MaterialTheme.typography.bodyLarge)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onLocationSelected(SelectedLocation(label = selectedLabel, point = selectedPoint)) },
            ) {
                Text(selectActionText)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

data class SelectedLocation(
    val label: String,
    val point: GeoPoint,
)
