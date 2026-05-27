package org.noztek.esktransport.feature.common.forgot_password.presentation

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
