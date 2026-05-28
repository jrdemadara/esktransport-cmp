package org.noztek.esktransport.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosCurrentLocationProvider : CurrentLocationProvider {
    private val locationManager = CLLocationManager()

    override suspend fun getLastKnownLocation(): GeoPoint? {
        val location = resolveLocation() ?: return null
        return location.coordinate.useContents {
            GeoPoint(latitude = latitude, longitude = longitude)
        }
    }

    override suspend fun getCurrentLocationLabel(): String? {
        val location = resolveLocation() ?: return null
        val fallbackLabel = location.coordinate.useContents {
            "Current Location (${latitude.formatCoordinate()}, ${longitude.formatCoordinate()})"
        }
        return reverseGeocode(location) ?: fallbackLabel
    }

    private suspend fun resolveLocation(): CLLocation? {
        val cachedLocation = locationManager.location
        if (cachedLocation != null) return cachedLocation

        val status: CLAuthorizationStatus = CLLocationManager.authorizationStatus()
        if (status == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
            return null
        }
        if (status != kCLAuthorizationStatusAuthorizedWhenInUse && status != kCLAuthorizationStatusAuthorizedAlways) {
            return null
        }

        return requestSingleLocation()
    }

    private suspend fun requestSingleLocation(): CLLocation? = suspendCancellableCoroutine { continuation ->
        val delegate = SingleLocationDelegate(
            onLocation = { location ->
                if (continuation.isActive) continuation.resume(location)
            },
            onFailure = {
                if (continuation.isActive) continuation.resume(null)
            },
        )
        locationManager.delegate = delegate
        continuation.invokeOnCancellation {
            locationManager.delegate = null
        }
        locationManager.requestLocation()
    }

    private suspend fun reverseGeocode(location: CLLocation): String? = suspendCancellableCoroutine { continuation ->
        CLGeocoder().reverseGeocodeLocation(location) { placemarks: List<*>?, _: NSError? ->
            val placemark = placemarks?.firstOrNull() as? platform.CoreLocation.CLPlacemark
            val primary = listOfNotNull(
                placemark?.name,
                placemark?.thoroughfare,
            ).distinct().joinToString(", ").ifBlank { null }
            val locality = listOfNotNull(
                placemark?.subLocality,
                placemark?.locality,
            ).distinct().joinToString(", ").ifBlank { null }
            val label = listOfNotNull(primary, locality).joinToString(" - ").ifBlank { null }
            if (continuation.isActive) continuation.resume(label)
        }
    }

    private class SingleLocationDelegate(
        private val onLocation: (CLLocation?) -> Unit,
        private val onFailure: () -> Unit,
    ) : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            onLocation(didUpdateLocations.filterIsInstance<CLLocation>().lastOrNull())
            manager.delegate = null
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            onFailure()
            manager.delegate = null
        }
    }

    private fun Double.formatCoordinate(): String = ((this * 100000).toLong() / 100000.0).toString()
}
