package org.noztek.esktransport.feature.common.otp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpRequestDto(
    @SerialName("phone")
    val phone: String,
    @SerialName("purpose")
    val purpose: String = "register",
)
