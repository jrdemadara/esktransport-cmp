package org.noztek.esktransport.app.di

expect class PlatformKoinContext

expect fun createPlatformKoinContext(
    rawContext: Any? = null,
    pusherAppKey: String = "",
    pusherAppCluster: String = "",
    pusherAuthEndpoint: String = "",
    mapboxAccessToken: String = "",
): PlatformKoinContext

expect fun initKoinPlatform(context: PlatformKoinContext)
