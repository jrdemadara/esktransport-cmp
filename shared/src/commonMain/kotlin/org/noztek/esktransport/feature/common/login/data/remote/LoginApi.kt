package org.noztek.esktransport.feature.common.login.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import org.noztek.esktransport.feature.common.login.data.remote.dto.LoginRequestDto
import org.noztek.esktransport.feature.common.login.data.remote.dto.LoginResponseDto

class LoginApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(request: LoginRequestDto): LoginResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
