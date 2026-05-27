package org.noztek.esktransport.core.realtime

import org.koin.mp.KoinPlatformTools
import org.noztek.esktransport.core.session.domain.usecase.GetCachedSessionTokenUseCase

object IosAuthTokenBridge {
    fun cachedToken(): String? {
        val koin = runCatching { KoinPlatformTools.defaultContext().get() }.getOrNull() ?: return null
        return runCatching {
            koin.get<GetCachedSessionTokenUseCase>().invoke()
        }.getOrNull()
    }
}
