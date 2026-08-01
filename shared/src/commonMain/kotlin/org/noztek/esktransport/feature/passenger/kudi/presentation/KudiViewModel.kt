package org.noztek.esktransport.feature.passenger.kudi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversation
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.CreateKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.GetCurrentKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.SendKudiMessageUseCase

class KudiViewModel(
    private val getCurrentKudiSessionUseCase: GetCurrentKudiSessionUseCase,
    private val createKudiSessionUseCase: CreateKudiSessionUseCase,
    private val sendKudiMessageUseCase: SendKudiMessageUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(KudiUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadSession()
    }

    fun sendMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }

            val sessionId = uiState.value.sessionPublicId ?: createSessionForSend()

            if (sessionId == null) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = "Unable to start Kudi.",
                    )
                }
                return@launch
            }

            val result = withContext(ioDispatcher) {
                sendKudiMessageUseCase(sessionPublicId = sessionId, message = trimmed)
            }

            result
                .onSuccess { messageResult ->
                    _uiState.update {
                        it.copy(
                            sessionPublicId = messageResult.conversation.session?.publicId ?: sessionId,
                            messages = messageResult.conversation.messages,
                            isSending = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = throwable.message ?: "Unable to send message.",
                        )
                    }
                }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { createKudiSessionUseCase() }
            result
                .onSuccess { conversation -> applyConversation(conversation, isLoading = false) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to start Kudi.",
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getCurrentKudiSessionUseCase() }
            result
                .onSuccess { conversation -> applyConversation(conversation, isLoading = false) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load Kudi.",
                        )
                    }
                }
        }
    }

    private suspend fun createSessionForSend(): String? {
        val result = withContext(ioDispatcher) { createKudiSessionUseCase() }
        return result.getOrNull()?.also { conversation ->
            applyConversation(conversation, isLoading = false)
        }?.session?.publicId
    }

    private fun applyConversation(conversation: KudiConversation, isLoading: Boolean) {
        _uiState.update {
            it.copy(
                sessionPublicId = conversation.session?.publicId,
                messages = conversation.messages,
                isLoading = isLoading,
                isSending = false,
                errorMessage = null,
            )
        }
    }
}
