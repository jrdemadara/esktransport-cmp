package org.noztek.esktransport.core.network

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(
    json: Json,
    authTokenProvider: () -> String?,
): HttpClient
