package org.noztek.esktransport.app.di

import org.koin.dsl.module
import org.noztek.esktransport.core.location.CurrentLocationProvider
import org.noztek.esktransport.core.location.IosCurrentLocationProvider
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.network.NetworkConfig
import org.noztek.esktransport.core.realtime.RealtimeConfig
import org.noztek.esktransport.core.storage.createPlatformSettings

actual class PlatformKoinContext internal constructor(
    internal val realtimeConfig: RealtimeConfig,
    internal val mapboxConfig: MapboxConfig,
)

actual fun createPlatformKoinContext(
    rawContext: Any?,
    pusherAppKey: String,
    pusherAppCluster: String,
    pusherAuthEndpoint: String,
    mapboxAccessToken: String,
): PlatformKoinContext = PlatformKoinContext(
    realtimeConfig = RealtimeConfig(
        appKey = pusherAppKey,
        cluster = pusherAppCluster,
        authEndpoint = pusherAuthEndpoint.toRealtimeAuthUrl(),
    ),
    mapboxConfig = MapboxConfig(accessToken = mapboxAccessToken),
)

private fun String.toRealtimeAuthUrl(): String {
    return if (startsWith("http://") || startsWith("https://")) {
        this
    } else {
        NetworkConfig.API_BASE_URL.trimEnd('/') + "/" + trimStart('/')
    }
}

private var isKoinStarted = false

actual fun initKoinPlatform(context: PlatformKoinContext) {
    if (isKoinStarted) return
    initKoin(
        extraModules = listOf(
            module {
                single { createPlatformSettings(context) }
                single { context.realtimeConfig }
                single { context.mapboxConfig }
                single<CurrentLocationProvider> { IosCurrentLocationProvider() }
            }
        )
    )
    isKoinStarted = true
}
