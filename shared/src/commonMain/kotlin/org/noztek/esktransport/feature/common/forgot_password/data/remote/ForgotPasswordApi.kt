package org.noztek.esktransport.feature.common.forgot_password.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.forgot_password.data.remote.dto.ForgotPasswordRequestDto
import org.noztek.esktransport.feature.common.forgot_password.data.remote.dto.ForgotPasswordResponseDto

class ForgotPasswordApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun forgotPassword(request: ForgotPasswordRequestDto): ForgotPasswordResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
