package org.noztek.esktransport.feature.common.reset_password.domain.model

data class ResetPasswordPayload(
    val phone: String,
    val resetToken: String,
    val password: String,
    val passwordConfirmation: String
)
