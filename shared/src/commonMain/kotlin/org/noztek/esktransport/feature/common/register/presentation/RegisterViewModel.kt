package org.noztek.esktransport.feature.common.register.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import org.noztek.esktransport.feature.common.register.domain.model.RegisterPayload
import org.noztek.esktransport.feature.common.register.domain.model.RegisterRole
import org.noztek.esktransport.feature.common.register.domain.usecase.RegisterUserUseCase
import kotlin.time.Clock

class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
    private val otpStateStore: OtpStateStore,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        role: RegisterRole,
    ) {
        if (password != passwordConfirmation) {
            _state.update { it.copy(errorMessage = "Password confirmation does not match.") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            val normalizedPhone = phone.trim()
            _state.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    isRegistered = false,
                    registeredPhone = null,
                )
            }

            val result = registerUserUseCase(
                RegisterPayload(
                    name = name.trim(),
                    phone = normalizedPhone,
                    email = email.trim().ifBlank { null },
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                    role = role,
                ),
            )

            result.fold(
                onSuccess = {
                    otpStateStore.setPendingPhone(normalizedPhone)
                    otpStateStore.setPendingPurpose("register")
                    otpStateStore.setResendAvailableAtMs(Clock.System.now().toEpochMilliseconds() + RegisterViewModel.Companion.RESEND_COOLDOWN_MS)
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isRegistered = true,
                            registeredPhone = normalizedPhone,
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "Registration failed.",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private companion object {
        private const val RESEND_COOLDOWN_MS = 30_000L
    }
}
