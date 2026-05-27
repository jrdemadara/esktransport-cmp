package org.noztek.esktransport.feature.common.otp.presentation

data class OtpUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isVerified: Boolean = false,
    val resendCooldownSeconds: Int = 30,
    val resetToken: String? = null
)
