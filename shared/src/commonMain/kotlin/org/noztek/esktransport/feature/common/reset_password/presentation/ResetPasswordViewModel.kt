package org.noztek.esktransport.feature.common.reset_password.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import org.noztek.esktransport.feature.common.reset_password.domain.model.ResetPasswordPayload
import org.noztek.esktransport.feature.common.reset_password.domain.usecase.ResetPasswordUseCase
import kotlin.time.Clock

class ResetPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val otpStateStore: OtpStateStore,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()

    fun resetPassword(phone: String,
                      token: String,
                      password: String,
                      passwordConfirmation: String) {
        if (password != passwordConfirmation) {
            _state.update { it.copy(errorMessage = "Password confirmation does not match.") }
            return
        }

        if (password.length < 8) {
            _state.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            val normalizedPhone = phone.trim()
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = resetPasswordUseCase(
                ResetPasswordPayload(
                    phone = phone,
                    resetToken = token,
                    password = password,
                    passwordConfirmation = passwordConfirmation
                )
            )
            
            result.fold(
                onSuccess = {
                    otpStateStore.setPendingPhone(normalizedPhone)
                    otpStateStore.setResendAvailableAtMs(Clock.System.now().toEpochMilliseconds() + ResetPasswordViewModel.Companion.RESEND_COOLDOWN_MS)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            resetPhone = normalizedPhone
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = throwable.message ?: "Reset failed"
                        ) 
                    }
                }
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
