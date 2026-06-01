package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig

@Composable
expect fun DriverTurnByTurnHost(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: MapPoint,
    destinationPoint: MapPoint,
    routePoints: List<MapPoint>,
    pickupConfirmed: Boolean,
)
