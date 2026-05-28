package org.noztek.esktransport.core.permission

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun RequestLocationPermissionIfNeeded() {
    val context = LocalContext.current
    val permissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {}
    val missingPermissions = remember(context, permissions) {
        permissions.filter { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
    }

    LaunchedEffect(missingPermissions.contentToString()) {
        if (missingPermissions.isNotEmpty()) {
            launcher.launch(missingPermissions)
        }
    }
}
