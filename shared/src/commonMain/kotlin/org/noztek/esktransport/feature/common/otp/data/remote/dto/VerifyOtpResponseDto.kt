package org.noztek.esktransport.feature.common.otp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponseDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: org.noztek.esktransport.feature.common.otp.data.remote.dto.VerifyOtpDataDto? = null
)

@Serializable
data class VerifyOtpDataDto(
    @SerialName("phone_verified_at")
    val phoneVerifiedAt: String,
    @SerialName("roles")
    val roles: List<String>,
    @SerialName("reset_token")
    val resetToken: String? = null
)
