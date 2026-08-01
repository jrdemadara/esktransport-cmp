package org.noztek.esktransport.feature.passenger.kudi.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiMessageResponseDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiSessionResponseDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.SendKudiMessageRequestDto

class KudiApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private companion object {
        const val KUDI_MESSAGE_TIMEOUT_MS = 90_000L
    }

    private val rootUrl = "${baseUrl.trimEnd('/')}/api/v1/passenger/kudi"

    suspend fun getCurrentSession(): KudiSessionResponseDto {
        return client.get("$rootUrl/sessions/current").body()
    }

    suspend fun createSession(): KudiSessionResponseDto {
        return client.post("$rootUrl/sessions").body()
    }

    suspend fun sendMessage(sessionPublicId: String, message: String): KudiMessageResponseDto {
        return client.post("$rootUrl/sessions/$sessionPublicId/messages") {
            timeout {
                requestTimeoutMillis = KUDI_MESSAGE_TIMEOUT_MS
                socketTimeoutMillis = KUDI_MESSAGE_TIMEOUT_MS
            }
            contentType(ContentType.Application.Json)
            setBody(SendKudiMessageRequestDto(message = message))
        }.body()
    }
}
