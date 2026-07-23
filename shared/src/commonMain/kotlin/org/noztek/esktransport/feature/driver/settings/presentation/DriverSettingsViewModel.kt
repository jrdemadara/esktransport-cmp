package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.feature.common.logout.domain.usecase.LogoutUseCase

class DriverSettingsViewModel(
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverSettingsUiState())
    val uiState: StateFlow<DriverSettingsUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            logoutUseCase().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLoggedOut = true,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = throwable.message ?: "Logout failed.",
                        )
                    }
                },
            )
        }
    }

    fun clearLogoutState() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeCurrentSessionUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        name = user.name.orEmpty(),
                        phone = user.phone.orEmpty(),
                        role = user.primaryRole?.replaceFirstChar { char -> char.uppercase() } ?: "Driver",
                    )
                }
            }
        }
    }
}
