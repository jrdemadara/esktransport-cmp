package org.noztek.esktransport.app.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.noztek.esktransport.core.location.AndroidCurrentLocationProvider
import org.noztek.esktransport.core.location.CurrentLocationProvider
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.network.NetworkConfig
import org.noztek.esktransport.core.platform.AppBuildInfo
import org.noztek.esktransport.core.realtime.RealtimeConfig
import org.noztek.esktransport.core.storage.createPlatformSettings

actual class PlatformKoinContext internal constructor(
    internal val appContext: Context,
    internal val realtimeConfig: RealtimeConfig,
    internal val mapboxConfig: MapboxConfig,
    internal val appBuildInfo: AppBuildInfo,
)

actual fun createPlatformKoinContext(
    rawContext: Any?,
    pusherAppKey: String,
    pusherAppCluster: String,
    pusherAuthEndpoint: String,
    mapboxAccessToken: String,
    appVersionName: String,
): PlatformKoinContext {
    require(rawContext is Context) { "Android Koin init requires android.content.Context" }
    return PlatformKoinContext(
        appContext = rawContext.applicationContext,
        realtimeConfig = RealtimeConfig(
            appKey = pusherAppKey,
            cluster = pusherAppCluster,
            authEndpoint = pusherAuthEndpoint.toRealtimeAuthUrl(),
        ),
        mapboxConfig = MapboxConfig(accessToken = mapboxAccessToken),
        appBuildInfo = AppBuildInfo(versionName = appVersionName),
    )
}

private fun String.toRealtimeAuthUrl(): String {
    return if (startsWith("http://") || startsWith("https://")) {
        this
    } else {
        NetworkConfig.API_BASE_URL.trimEnd('/') + "/" + trimStart('/')
    }
}

actual fun initKoinPlatform(context: PlatformKoinContext) {
    if (GlobalContext.getOrNull() != null) return

    initKoin {
        androidLogger(Level.INFO)
        androidContext(context.appContext)
        modules(
            module {
                single { createPlatformSettings(context) }
                single { context.realtimeConfig }
                single { context.mapboxConfig }
                single { context.appBuildInfo }
                single<CurrentLocationProvider> { AndroidCurrentLocationProvider(context.appContext) }
            }
        )
    }
}
