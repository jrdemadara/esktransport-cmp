package org.noztek.esktransport.feature.common.otp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.common.otp.data.local.OtpStateStore
import org.noztek.esktransport.feature.common.otp.domain.usecase.RequestOtpUseCase
import org.noztek.esktransport.feature.common.otp.domain.usecase.VerifyOtpUseCase
import kotlin.time.Clock

class OtpViewModel(
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val stateStore: OtpStateStore,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var resendCooldownJob: Job? = null
    private val _state = MutableStateFlow(OtpUiState())
    val state: StateFlow<OtpUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            val persistedAvailableAt = stateStore.getResendAvailableAtMs()
            val now = Clock.System.now().toEpochMilliseconds()
            val remainingSeconds = persistedAvailableAt
                ?.let { ((it - now + 999L) / 1000L).toInt() }
                ?.coerceAtLeast(0)
                ?: 0

            startResendCooldown(remainingSeconds)
        }
    }

    fun verifyOtp(phone: String, otpCode: String, purpose: String) {
        if (otpCode.isBlank()) {
            _state.update { it.copy(errorMessage = "OTP code is required.", infoMessage = null) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null, isVerified = false) }

            val result = verifyOtpUseCase(phone = phone, otpCode = otpCode.trim(), purpose = purpose)
            result.fold(
                onSuccess = { resetToken ->
                    stateStore.clearPendingState()
                    _state.update { 
                        it.copy(
                            isSubmitting = false, 
                            isVerified = true, 
                            infoMessage = null,
                            resetToken = resetToken
                        ) 
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "OTP verification failed.",
                            infoMessage = null,
                        )
                    }
                },
            )
        }
    }

    fun leaveOtpFlow() {
        viewModelScope.launch(ioDispatcher) {
            stateStore.clearPendingState()
        }
    }

    fun resendOtp(phone: String, purpose: String) {
        if (_state.value.resendCooldownSeconds > 0 || _state.value.isSubmitting) {
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _state.update { it.copy(isSubmitting = true, errorMessage = null, infoMessage = null) }
            val result = requestOtpUseCase(phone = phone.trim(), purpose = purpose)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = null,
                            infoMessage = "A new OTP has been sent.",
                        )
                    }
                    val availableAt = Clock.System.now().toEpochMilliseconds() + RESEND_COOLDOWN_MS
                    stateStore.setResendAvailableAtMs(availableAt)
                    startResendCooldown(RESEND_COOLDOWN_SECONDS)
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = throwable.message ?: "Resend OTP failed.",
                            infoMessage = null,
                        )
                    }
                },
            )
        }
    }

    private fun startResendCooldown(initialSeconds: Int) {
        resendCooldownJob?.cancel()
        resendCooldownJob = viewModelScope.launch {
            _state.update { it.copy(resendCooldownSeconds = initialSeconds.coerceAtLeast(0)) }
            while (_state.value.resendCooldownSeconds > 0) {
                delay(1000)
                _state.update { current ->
                    current.copy(resendCooldownSeconds = (current.resendCooldownSeconds - 1).coerceAtLeast(0))
                }
            }
            stateStore.clearResendAvailableAtMs()
        }
    }

    companion object {
        private const val RESEND_COOLDOWN_SECONDS = 30
        private const val RESEND_COOLDOWN_MS = RESEND_COOLDOWN_SECONDS * 1000L
    }
}
