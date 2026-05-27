package org.noztek.esktransport.feature.common.login.presentation

data class LoginUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isLogin: Boolean = false,
)