package org.noztek.esktransport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.noztek.esktransport.app.di.createPlatformKoinContext
import org.noztek.esktransport.app.di.initKoinPlatform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        initKoinPlatform(
            createPlatformKoinContext(
                rawContext = applicationContext,
                pusherAppKey = BuildConfig.PUSHER_APP_KEY,
                pusherAppCluster = BuildConfig.PUSHER_APP_CLUSTER,
                pusherAuthEndpoint = BuildConfig.PUSHER_AUTH_ENDPOINT,
                mapboxAccessToken = getString(R.string.mapbox_access_token),
            )
        )
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
