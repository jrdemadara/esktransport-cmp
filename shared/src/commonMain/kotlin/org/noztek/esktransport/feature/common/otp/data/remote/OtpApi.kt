package org.noztek.esktransport.feature.common.otp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.otp.data.remote.dto.RequestOtpRequestDto
import org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpRequestDto
import org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpResponseDto

class OtpApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun requestOtp(request: org.noztek.esktransport.feature.common.otp.data.remote.dto.RequestOtpRequestDto) {
        client.post("${baseUrl.trimEnd('/')}/api/v1/auth/request-otp") {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun verifyOtp(request: org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpRequestDto): org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/auth/verify-otp") {
            expectSuccess = true
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
