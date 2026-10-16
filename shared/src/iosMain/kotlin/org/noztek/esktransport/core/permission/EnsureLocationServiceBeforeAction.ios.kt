package org.noztek.esktransport.core.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun EnsureLocationServiceBeforeAction(
    requested: Boolean,
    settingsConfirmed: Boolean,
    onReady: () -> Unit,
    onSettingsRequired: () -> Unit,
    onUnavailable: () -> Unit,
) {
    LaunchedEffect(requested, settingsConfirmed) {
        if (!requested) return@LaunchedEffect
        val status: CLAuthorizationStatus = CLLocationManager.authorizationStatus()
        if (status == kCLAuthorizationStatusNotDetermined) {
            CLLocationManager().requestWhenInUseAuthorization()
            onUnavailable()
            return@LaunchedEffect
        }

        val authorized = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
        if (authorized && CLLocationManager.locationServicesEnabled()) {
            onReady()
        } else if (!settingsConfirmed) {
            onSettingsRequired()
        } else {
            UIApplication.sharedApplication.openURL(NSURL(string = "app-settings:"))
            onUnavailable()
        }
    }
}
