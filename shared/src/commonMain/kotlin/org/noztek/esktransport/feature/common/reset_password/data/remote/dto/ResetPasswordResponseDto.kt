package org.noztek.esktransport.feature.common.reset_password.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordResponseDto(
    val message: String
)
