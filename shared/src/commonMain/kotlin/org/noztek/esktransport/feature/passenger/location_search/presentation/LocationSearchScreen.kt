package org.noztek.esktransport.feature.passenger.location_search.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import asktransport_cmp.shared.generated.resources.Res
import asktransport_cmp.shared.generated.resources.map_pin_black
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
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
    var cameraCenter by remember { mutableStateOf(MapPoint(selectedPoint.latitude, selectedPoint.longitude)) }
    val mapCameraDefaults = remember { MapCameraDefaults(zoom = 15.5, pitch = 30.0) }
    val selectedLabel = state.tappedLocationLabel ?: "Move or search to pick a location"
    val pinLift by animateDpAsState(
        targetValue = if (state.isMapMoving) (-12).dp else 0.dp,
        animationSpec = if (state.isMapMoving) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "location-picker-pin-lift",
    )

    LaunchedEffect(normalizedMode) { viewModel.onScreenOpened() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LocationSearchUiEvent.MoveCamera -> {
                    cameraCenter = MapPoint(event.point.latitude, event.point.longitude)
                }
            }
        }
    }

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
                cameraCenter = cameraCenter,
                cameraDefaults = mapCameraDefaults,
                onCameraMoving = { point ->
                    viewModel.onMapMoving(GeoPoint(point.latitude, point.longitude))
                },
                onCameraIdle = { point ->
                    viewModel.onMapSettled(GeoPoint(point.latitude, point.longitude))
                },
            )
            Image(
                painter = painterResource(Res.drawable.map_pin_black),
                contentDescription = "Selected location",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 44.dp)
                    .offset(y = pinLift)
                    .size(64.dp),
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                selectedLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
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
