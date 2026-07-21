package org.noztek.esktransport.feature.common.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.chat.domain.model.SendTripChatMessagePayload
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole
import org.noztek.esktransport.feature.common.chat.domain.usecase.GetTripChatMessagesUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.ObserveTripChatMessagesUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.SendTripChatMessageUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.SubscribeTripChatUseCase
import org.noztek.esktransport.feature.common.chat.domain.usecase.UnsubscribeTripChatUseCase

sealed class TripChatUiEvent {
    data class ShowSnackbar(val message: String) : TripChatUiEvent()
}

class TripChatViewModel(
    private val getTripChatMessagesUseCase: GetTripChatMessagesUseCase,
    private val sendTripChatMessageUseCase: SendTripChatMessageUseCase,
    private val subscribeTripChatUseCase: SubscribeTripChatUseCase,
    private val unsubscribeTripChatUseCase: UnsubscribeTripChatUseCase,
    private val observeTripChatMessagesUseCase: ObserveTripChatMessagesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<TripChatUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<TripChatUiEvent> = _uiEvents.asSharedFlow()

    private var realtimeJob: Job? = null

    fun open(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ) {
        val current = _uiState.value
        if (current.bookingPublicId == bookingPublicId && current.role == role && current.messages.isNotEmpty()) {
            subscribe(role)
            return
        }

        _uiState.update {
            TripChatUiState(
                bookingPublicId = bookingPublicId,
                role = role,
                draft = it.draft.takeIf { current.bookingPublicId == bookingPublicId }.orEmpty(),
                isLoading = true,
            )
        }
        subscribe(role)
        loadMessages(bookingPublicId, role)
    }

    fun onDraftChange(value: String) {
        _uiState.update {
            it.copy(
                draft = value.take(MaxMessageLength),
                errorMessage = null,
            )
        }
    }

    fun send() {
        val state = _uiState.value
        val message = state.draft.trim()
        if (message.isBlank() || state.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, draft = "", errorMessage = null) }
            val result = withContext(ioDispatcher) {
                sendTripChatMessageUseCase(
                    SendTripChatMessagePayload(
                        bookingPublicId = state.bookingPublicId,
                        role = state.role,
                        message = message,
                    ),
                )
            }
            result
                .onSuccess { sentMessage ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            messages = it.messages.mergeById(sentMessage),
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Message failed to send."
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            draft = message,
                            errorMessage = error,
                        )
                    }
                    _uiEvents.tryEmit(TripChatUiEvent.ShowSnackbar(error))
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        realtimeJob?.cancel()
        realtimeJob = null
        unsubscribeTripChatUseCase()
    }

    private fun loadMessages(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ) {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                getTripChatMessagesUseCase(bookingPublicId, role)
            }
            result
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages.sortedBy { message -> message.id },
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Unable to load chat."
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error,
                        )
                    }
                    _uiEvents.tryEmit(TripChatUiEvent.ShowSnackbar(error))
                }
        }
    }

    private fun subscribe(role: TripChatParticipantRole) {
        subscribeTripChatUseCase(role)
        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            observeTripChatMessagesUseCase().collectLatest { message ->
                val state = _uiState.value
                if (message.bookingPublicId != state.bookingPublicId) return@collectLatest
                _uiState.update {
                    it.copy(messages = it.messages.mergeById(message))
                }
            }
        }
    }

    override fun onCleared() {
        close()
        super.onCleared()
    }
}

private const val MaxMessageLength = 500

private fun List<TripChatMessage>.mergeById(message: TripChatMessage): List<TripChatMessage> {
    return (filterNot { it.id == message.id } + message).sortedBy { it.id }
}
