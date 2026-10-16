package org.noztek.esktransport.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun EnsureLocationServiceBeforeAction(
    requested: Boolean,
    settingsConfirmed: Boolean,
    onReady: () -> Unit,
    onSettingsRequired: () -> Unit,
    onUnavailable: () -> Unit,
) {
    val context = LocalContext.current
    var waitingForPermission by remember { mutableStateOf(false) }
    var waitingForSettings by remember { mutableStateOf(false) }
    var settingsPromptShown by remember { mutableStateOf(false) }

    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationServiceEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return runCatching {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        waitingForSettings = false
        if (isPermissionGranted() && isLocationServiceEnabled()) onReady() else onUnavailable()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        waitingForPermission = false
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!granted) {
            onUnavailable()
        } else if (isLocationServiceEnabled()) {
            onReady()
        } else {
            waitingForSettings = true
            settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    LaunchedEffect(requested, settingsConfirmed, waitingForPermission, waitingForSettings) {
        if (!requested) {
            settingsPromptShown = false
            waitingForPermission = false
            waitingForSettings = false
            return@LaunchedEffect
        }
        if (waitingForPermission || waitingForSettings) return@LaunchedEffect

        if (!isPermissionGranted()) {
            waitingForPermission = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else if (isLocationServiceEnabled()) {
            onReady()
        } else if (!settingsConfirmed) {
            if (!settingsPromptShown) {
                settingsPromptShown = true
                onSettingsRequired()
            }
        } else {
            waitingForSettings = true
            settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
}
