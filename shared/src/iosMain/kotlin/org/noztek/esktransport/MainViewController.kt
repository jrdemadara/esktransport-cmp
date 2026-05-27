package org.noztek.esktransport

import androidx.compose.ui.window.ComposeUIViewController
import org.noztek.esktransport.app.di.createPlatformKoinContext
import org.noztek.esktransport.app.di.initKoinPlatform

fun MainViewController(
    mapboxAccessToken: String = "",
) = ComposeUIViewController {
    initKoinPlatform(
        createPlatformKoinContext(
            mapboxAccessToken = mapboxAccessToken,
        )
    )
    App()
}
