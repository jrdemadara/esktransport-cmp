package org.noztek.esktransport

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.ui.uikit.OnFocusBehavior
import org.noztek.esktransport.app.di.createPlatformKoinContext
import org.noztek.esktransport.app.di.initKoinPlatform

fun MainViewController(
    mapboxAccessToken: String = "",
) = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) {
    initKoinPlatform(
        createPlatformKoinContext(
            mapboxAccessToken = mapboxAccessToken,
        )
    )
    App()
}
