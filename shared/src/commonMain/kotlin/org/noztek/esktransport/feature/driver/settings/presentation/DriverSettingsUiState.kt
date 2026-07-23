package org.noztek.esktransport.feature.driver.settings.presentation

data class DriverSettingsUiState(
    val name: String = "",
    val phone: String = "",
    val role: String = "Driver",
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
)
