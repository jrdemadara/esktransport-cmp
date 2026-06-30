package org.noztek.esktransport.feature.driver.trip_navigation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import platform.UIKit.UIView

@Composable
actual fun DriverTurnByTurnHost(
    modifier: Modifier,
    mapboxConfig: MapboxConfig,
    pickupPoint: MapPoint,
    destinationPoint: MapPoint,
    routePoints: List<MapPoint>,
    pickupConfirmed: Boolean,
    onLocationChanged: (DriverNavigationLocation) -> Unit,
) {
    val currentOnLocationChanged = rememberUpdatedState(onLocationChanged)
    val locationListener = remember {
        object : IosDriverNavigationLocationListener {
            override fun onLocationChanged(location: DriverNavigationLocation) {
                currentOnLocationChanged.value(location)
            }
        }
    }
    val request = remember(mapboxConfig, pickupPoint, destinationPoint, routePoints, pickupConfirmed) {
        IosDriverNavigationRequest(
            accessToken = mapboxConfig.accessToken,
            pickupLatitude = pickupPoint.latitude,
            pickupLongitude = pickupPoint.longitude,
            destinationLatitude = destinationPoint.latitude,
            destinationLongitude = destinationPoint.longitude,
            routePoints = routePoints,
            pickupConfirmed = pickupConfirmed,
            locationListener = locationListener,
        )
    }

    val fallback = remember { UIView() }
    UIKitView(
        modifier = modifier,
        factory = { IosDriverNavigationBridge.createNavigationView(request) ?: fallback },
        update = { view -> IosDriverNavigationBridge.updateNavigationView(view, request) },
    )
}
