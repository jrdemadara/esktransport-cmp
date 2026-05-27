package org.noztek.esktransport.feature.common.otp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequestDto(
    @SerialName("phone")
    val phone: String,
    @SerialName("otp_code")
    val otpCode: String,
    @SerialName("purpose")
    val purpose: String = "register",
)
