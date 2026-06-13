package org.noztek.esktransport

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import org.noztek.esktransport.app.di.createPlatformKoinContext
import org.noztek.esktransport.app.di.initKoinPlatform

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
            )
        )
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
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
