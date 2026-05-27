package org.noztek.esktransport.app.di.modules

import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noztek.esktransport.core.network.NetworkConfig
import org.noztek.esktransport.core.network.createPlatformHttpClient
import org.noztek.esktransport.core.session.SessionManager

const val API_BASE_URL_QUALIFIER = "api_base_url"

val networkModule = module {
    single(named(API_BASE_URL_QUALIFIER)) { NetworkConfig.API_BASE_URL }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single {
        createPlatformHttpClient(
            json = get(),
            authTokenProvider = { get<SessionManager>().cachedToken() },
        )
    }
}
