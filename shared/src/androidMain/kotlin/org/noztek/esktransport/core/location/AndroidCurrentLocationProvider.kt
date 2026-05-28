package org.noztek.esktransport.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import kotlin.coroutines.resume

class AndroidCurrentLocationProvider(
    private val context: Context,
) : CurrentLocationProvider {
    private val locationManager: LocationManager?
        get() = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    override suspend fun getLastKnownLocation(): GeoPoint? {
        return resolveLocation()?.toGeoPoint()
    }

    override suspend fun getCurrentLocationLabel(): String? {
        val location = resolveLocation() ?: return null
        return reverseGeocode(location) ?: location.currentLocationLabel()
    }

    @SuppressLint("MissingPermission")
    private suspend fun resolveLocation(): Location? {
        if (!hasLocationPermission()) return null
        return getBestCachedLocation() ?: requestOneShotLocationWithTimeout()
    }

    private fun hasLocationPermission(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getBestCachedLocation(): Location? {
        val manager = locationManager ?: return null
        return enabledProviders(manager)
            .asSequence()
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { location -> location.time }
    }

    private suspend fun requestOneShotLocationWithTimeout(): Location? {
        return withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            requestOneShotLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestOneShotLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val manager = locationManager
        val provider = manager?.let(::enabledProviders)?.firstOrNull()
        if (manager == null || provider == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (continuation.isActive) continuation.resume(location)
                manager.removeUpdates(this)
            }

            @Deprecated("Deprecated in Android framework.")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) {
                if (continuation.isActive) continuation.resume(null)
                manager.removeUpdates(this)
            }
        }

        continuation.invokeOnCancellation {
            manager.removeUpdates(listener)
        }
        runCatching {
            manager.requestLocationUpdates(
                provider,
                MIN_LOCATION_UPDATE_INTERVAL_MS,
                MIN_LOCATION_UPDATE_DISTANCE_M,
                listener,
                Looper.getMainLooper(),
            )
        }.onFailure {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    private fun reverseGeocode(location: Location): String? {
        return runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.let { address ->
                    val primary = listOfNotNull(
                        address.featureName,
                        address.thoroughfare,
                    ).distinct().joinToString(", ").ifBlank { null }
                    val locality = listOfNotNull(
                        address.subLocality,
                        address.locality,
                    ).distinct().joinToString(", ").ifBlank { null }

                    listOfNotNull(primary, locality).joinToString(" - ").ifBlank { null }
                }
        }.getOrNull()
    }

    private fun enabledProviders(locationManager: LocationManager): List<String> {
        val enabledProviderSet = runCatching { locationManager.getProviders(true).toSet() }.getOrDefault(emptySet())
        val preferredProviders = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }
        val preferredEnabledProviders = preferredProviders.filter { provider ->
            provider in enabledProviderSet || runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        return (preferredEnabledProviders + enabledProviderSet.filterNot { it in preferredEnabledProviders }).distinct()
    }

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude = latitude, longitude = longitude)

    private fun Location.currentLocationLabel(): String {
        return "Current Location (${latitude.formatCoordinate()}, ${longitude.formatCoordinate()})"
    }

    private fun Double.formatCoordinate(): String = ((this * 100000).toLong() / 100000.0).toString()

    private companion object {
        const val LOCATION_TIMEOUT_MS = 10_000L
        const val MIN_LOCATION_UPDATE_INTERVAL_MS = 0L
        const val MIN_LOCATION_UPDATE_DISTANCE_M = 0f
    }
}
