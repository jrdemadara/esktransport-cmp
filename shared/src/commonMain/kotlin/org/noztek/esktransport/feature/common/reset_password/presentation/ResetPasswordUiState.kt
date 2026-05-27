package org.noztek.esktransport.feature.common.reset_password.presentation

data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val resetPhone: String? = null,
)
