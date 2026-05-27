package org.noztek.esktransport.feature.common.logout.presentation

data class LogoutUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)
