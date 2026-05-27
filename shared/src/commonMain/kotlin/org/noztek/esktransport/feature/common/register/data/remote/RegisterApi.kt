package org.noztek.esktransport.feature.common.register.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.register.data.remote.dto.RegisterRequestDto

class RegisterApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun register(request: org.noztek.esktransport.feature.common.register.data.remote.dto.RegisterRequestDto) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/auth/register") {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
