package org.noztek.esktransport.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined

@Composable
actual fun RequestLocationPermissionIfNeeded() {
    val locationManager = remember { CLLocationManager() }

    LaunchedEffect(Unit) {
        val status: CLAuthorizationStatus = CLLocationManager.authorizationStatus()
        if (status == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
        }
    }
}
