package org.noztek.esktransport.feature.common.forgot_password.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.forgot_password.domain.model.ForgotPasswordPayload
import org.noztek.esktransport.feature.common.forgot_password.domain.usecase.ForgotPasswordUseCase
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import kotlin.time.Clock

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val otpStateStore: OtpStateStore,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    fun forgotPassword(phone: String) {
        if (phone.isBlank()) {
            _state.update { it.copy(error = "Phone number is required.") }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            val normalizedPhone = phone.trim()
            _state.update { it.copy(isLoading = true, error = null) }
            val result = forgotPasswordUseCase(
                ForgotPasswordPayload(
                    phone = normalizedPhone
                )
            )
            
            result.fold(
                onSuccess = {
                    val cooldownMs = 30 * 1000L
                    otpStateStore.setPendingPhone(normalizedPhone)
                    otpStateStore.setPendingPurpose("reset_password")
                    otpStateStore.setResendAvailableAtMs(Clock.System.now().toEpochMilliseconds() + cooldownMs)
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false, 
                            error = throwable.message ?: "Failed to request reset"
                        ) 
                    }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
