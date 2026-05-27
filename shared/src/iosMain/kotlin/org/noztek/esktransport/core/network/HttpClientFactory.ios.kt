package org.noztek.esktransport.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createPlatformHttpClient(
    json: Json,
    authTokenProvider: () -> String?,
): HttpClient {
    return HttpClient(Darwin) {
        expectSuccess = true

        defaultRequest {
            authTokenProvider()
                ?.takeIf { it.isNotBlank() }
                ?.let { token -> header(HttpHeaders.Authorization, "Bearer $token") }
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = LogLevel.BODY
        }
    }
}
