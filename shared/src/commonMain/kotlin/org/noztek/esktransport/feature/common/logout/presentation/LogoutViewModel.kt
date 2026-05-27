package org.noztek.esktransport.feature.common.logout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.logout.domain.usecase.LogoutUseCase

class LogoutViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow(LogoutUiState())
    val state: StateFlow<LogoutUiState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = logoutUseCase()
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isLoggedOut = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Logout failed"
                        )
                    }
                }
            )
        }
    }

    fun resetState() {
        _state.update { LogoutUiState() }
    }
}
