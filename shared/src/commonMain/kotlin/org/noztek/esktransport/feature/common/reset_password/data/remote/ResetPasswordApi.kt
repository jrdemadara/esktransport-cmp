package org.noztek.esktransport.feature.common.reset_password.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.reset_password.data.remote.dto.ResetPasswordRequestDto
import org.noztek.esktransport.feature.common.reset_password.data.remote.dto.ResetPasswordResponseDto

class ResetPasswordApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun resetPassword(request: org.noztek.esktransport.feature.common.reset_password.data.remote.dto.ResetPasswordRequestDto): org.noztek.esktransport.feature.common.reset_password.data.remote.dto.ResetPasswordResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
