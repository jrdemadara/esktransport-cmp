package org.noztek.esktransport.feature.common.reset_password.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestDto(
    @SerialName("phone")
    val phone: String,
    @SerialName("reset_token")
    val resetToken: String,
    val password: String,
    @SerialName("password_confirmation")
    val passwordConfirmation: String,
)
