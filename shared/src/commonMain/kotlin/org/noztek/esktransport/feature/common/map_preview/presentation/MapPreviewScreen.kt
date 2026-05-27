package org.noztek.esktransport.feature.common.map_preview.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapMarker
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapRouteLine
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView

@Composable
fun MapPreviewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapboxConfig: MapboxConfig = koinInject(),
    cameraDefaults: MapCameraDefaults = koinInject(),
) {
    val pickup = MapPoint(latitude = 6.6920431660391095, longitude = 124.68050838312321)
    val destination = MapPoint(latitude = 6.676851920902377, longitude = 124.67557940440643)
    val center = MapPoint(
        latitude = (pickup.latitude + destination.latitude) / 2.0,
        longitude = (pickup.longitude + destination.longitude) / 2.0,
    )

    Box(modifier = modifier.fillMaxSize()) {
        PlatformMapView(
            modifier = Modifier.fillMaxSize(),
            config = mapboxConfig,
            cameraCenter = center,
            cameraDefaults = cameraDefaults.copy(zoom = 13.4, pitch = 0.0),
            markers = listOf(
                MapMarker(
                    id = "pickup",
                    point = pickup,
                    color = Color(0xFF2563EB),
                    radius = 8.0,
                ),
                MapMarker(
                    id = "destination",
                    point = destination,
                    color = Color(0xFFF59E0B),
                    radius = 8.0,
                ),
            ),
            routeLines = listOf(
                MapRouteLine(
                    id = "preview-route",
                    points = listOf(pickup, destination),
                    color = Color(0xFF2563EB),
                    width = 5.0,
                ),
            ),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 2.dp,
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(8.dp),
            ) {
                Text("Back")
            }
        }
    }
}
