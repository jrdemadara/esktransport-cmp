package org.noztek.esktransport

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.noztek.esktransport.app.di.createPlatformKoinContext
import org.noztek.esktransport.app.di.initKoinPlatform
import org.noztek.esktransport.core.notify.createPushNotificationTokenProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkMode = resources.configuration.isSystemInDarkMode()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkMode
            isAppearanceLightNavigationBars = !isDarkMode
        }

        initKoinPlatform(
            createPlatformKoinContext(
                rawContext = applicationContext,
                pusherAppKey = BuildConfig.PUSHER_APP_KEY,
                pusherAppCluster = BuildConfig.PUSHER_APP_CLUSTER,
                pusherAuthEndpoint = BuildConfig.PUSHER_AUTH_ENDPOINT,
                mapboxAccessToken = getString(R.string.mapbox_access_token),
                appVersionName = BuildConfig.VERSION_NAME,
            )
        )
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        logFirebaseMessagingToken()

        setContent {
            App()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            POST_NOTIFICATIONS_REQUEST_CODE,
        )
    }

    private fun logFirebaseMessagingToken() {
        lifecycleScope.launch {
            createPushNotificationTokenProvider().currentToken()
                .onSuccess { token -> Log.d(TAG, "FCM token: $token") }
                .onFailure { throwable -> Log.w(TAG, "Unable to get FCM token.", throwable) }
        }
    }

    private companion object {
        private const val TAG = "EskMainActivity"
        private const val POST_NOTIFICATIONS_REQUEST_CODE = 4101
    }
}

private fun Configuration.isSystemInDarkMode(): Boolean {
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
