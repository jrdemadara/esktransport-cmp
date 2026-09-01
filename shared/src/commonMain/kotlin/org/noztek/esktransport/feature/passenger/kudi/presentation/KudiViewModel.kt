package org.noztek.esktransport.feature.passenger.kudi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiChatMessage
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversation
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiMessageSender
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.CreateKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.GetCurrentKudiSessionUseCase
import org.noztek.esktransport.feature.passenger.kudi.domain.usecase.SendKudiMessageUseCase
import org.noztek.esktransport.feature.passenger.marketplace.domain.usecase.GetMarketplaceListingPhotoUseCase

class KudiViewModel(
    private val getCurrentKudiSessionUseCase: GetCurrentKudiSessionUseCase,
    private val createKudiSessionUseCase: CreateKudiSessionUseCase,
    private val sendKudiMessageUseCase: SendKudiMessageUseCase,
    private val getMarketplaceListingPhotoUseCase: GetMarketplaceListingPhotoUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(KudiUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    private var localMessageCounter = 0

    init {
        loadSession()
    }

    fun sendMessage(message: String) {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch {
            val localMessage = KudiChatMessage(
                id = "local-${++localMessageCounter}",
                sender = KudiMessageSender.User,
                message = trimmed,
                createdAt = null,
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + localMessage,
                    isSending = true,
                    errorMessage = null,
                )
            }

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
                        val publicIds = messageResult.conversation.messages.suggestionPublicIds().toSet()
                        it.copy(
                            sessionPublicId = messageResult.conversation.session?.publicId ?: sessionId,
                            messages = messageResult.conversation.messages,
                            vehiclePhotoBytes = it.vehiclePhotoBytes.filterKeys { publicId -> publicId in publicIds },
                            loadingPhotoPublicIds = it.loadingPhotoPublicIds.filter { publicId -> publicId in publicIds }.toSet(),
                            isSending = false,
                            errorMessage = null,
                        )
                    }
                    loadSuggestionPhotos(messageResult.conversation.messages)
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
        val conversation = result.getOrNull() ?: return null
        val sessionId = conversation.session?.publicId

        _uiState.update {
            it.copy(
                sessionPublicId = sessionId,
                isLoading = false,
                errorMessage = null,
            )
        }

        return sessionId
    }

    private fun applyConversation(conversation: KudiConversation, isLoading: Boolean) {
        _uiState.update {
            val publicIds = conversation.messages.suggestionPublicIds().toSet()
            it.copy(
                sessionPublicId = conversation.session?.publicId,
                messages = conversation.messages,
                vehiclePhotoBytes = it.vehiclePhotoBytes.filterKeys { publicId -> publicId in publicIds },
                loadingPhotoPublicIds = it.loadingPhotoPublicIds.filter { publicId -> publicId in publicIds }.toSet(),
                isLoading = isLoading,
                isSending = false,
                errorMessage = null,
            )
        }
        loadSuggestionPhotos(conversation.messages)
    }

    private fun loadSuggestionPhotos(messages: List<org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiChatMessage>) {
        messages
            .suggestionPublicIds()
            .filterNot { publicId -> _uiState.value.vehiclePhotoBytes.containsKey(publicId) }
            .filterNot { publicId -> _uiState.value.loadingPhotoPublicIds.contains(publicId) }
            .forEach { publicId ->
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(loadingPhotoPublicIds = it.loadingPhotoPublicIds + publicId)
                    }
                    val result = withContext(ioDispatcher) { getMarketplaceListingPhotoUseCase(publicId) }
                    _uiState.update {
                        val bytes = result.getOrNull()
                        it.copy(
                            vehiclePhotoBytes = if (bytes != null) {
                                it.vehiclePhotoBytes + (publicId to bytes)
                            } else {
                                it.vehiclePhotoBytes
                            },
                            loadingPhotoPublicIds = it.loadingPhotoPublicIds - publicId,
                        )
                    }
                }
            }
    }
}

private fun List<org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiChatMessage>.suggestionPublicIds(): List<String> {
    return flatMap { message -> message.vehicleSuggestions.map { suggestion -> suggestion.publicId } }
        .distinct()
}
