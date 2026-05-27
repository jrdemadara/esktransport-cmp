package org.noztek.esktransport.feature.common.register.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val name: String,
    val phone: String,
    val email: String? = null,
    val password: String,
    @SerialName("password_confirmation")
    val passwordConfirmation: String,
    val role: String,
)
