package org.noztek.esktransport.feature.common.forgot_password.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDto(
    @SerialName("phone")
    val phone: String
)

@Serializable
data class ForgotPasswordResponseDto(
    val message: String
)
