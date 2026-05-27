package org.noztek.esktransport.feature.common.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.login.domain.model.LoginPayload
import org.noztek.esktransport.feature.common.login.domain.usecase.LoginUseCase
import kotlin.time.Clock

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())

    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(
        phone: String,
        password: String,
    ) {
        val normalizedPhone = phone.trim()
        val normalizedPassword = password.trim()

        if (normalizedPhone.isBlank()) {
            _state.update { it.copy(errorMessage = "Phone is required.", isLogin = false) }
            return
        }

        if (normalizedPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Password is required.", isLogin = false) }
            return
        }

        val deviceName = "kmp-${Clock.System.now().toEpochMilliseconds()}"

        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isSubmitting = true, errorMessage = null, isLogin = false) }
            val result = loginUseCase(
                LoginPayload(
                    phone = normalizedPhone,
                    password = normalizedPassword,
                    deviceName = deviceName,
                ),
            )

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isSubmitting = false, isLogin = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "Login failed.",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
