package org.noztek.esktransport.feature.common.register.presentation

data class RegisterUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false,
    val registeredPhone: String? = null,
)
