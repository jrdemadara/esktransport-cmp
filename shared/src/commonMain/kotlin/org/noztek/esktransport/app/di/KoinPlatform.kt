package org.noztek.esktransport.app.di

expect class PlatformKoinContext

expect fun createPlatformKoinContext(
    rawContext: Any? = null,
    pusherAppKey: String = "",
    pusherAppCluster: String = "",
    pusherAuthEndpoint: String = "",
    mapboxAccessToken: String = "",
    appVersionName: String = "1.0.0",
): PlatformKoinContext

expect fun initKoinPlatform(context: PlatformKoinContext)
