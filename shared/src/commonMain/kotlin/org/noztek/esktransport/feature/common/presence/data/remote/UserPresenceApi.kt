package org.noztek.esktransport.feature.common.presence.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.presence.data.remote.dto.UserPresenceRequestDto
import org.noztek.esktransport.feature.common.presence.data.remote.dto.UserPresenceResponseDto

class UserPresenceApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getPresence(): UserPresenceResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/presence").body()
    }

    suspend fun heartbeat(request: UserPresenceRequestDto): UserPresenceResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/presence/heartbeat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun foreground(request: UserPresenceRequestDto): UserPresenceResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/presence/foreground") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun background(request: UserPresenceRequestDto): UserPresenceResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/presence/background") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun offline(request: UserPresenceRequestDto): UserPresenceResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/presence/offline") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
