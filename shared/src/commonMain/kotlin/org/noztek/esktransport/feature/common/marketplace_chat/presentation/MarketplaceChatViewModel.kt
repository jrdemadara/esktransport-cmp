package org.noztek.esktransport.feature.common.marketplace_chat.presentation

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
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatParticipantRole
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendOwnerMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.SendMarketplaceChatMessagePayload
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.GetOwnerMarketplaceMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.ObserveMarketplaceChatMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.OpenPassengerMarketplaceConversationUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SendOwnerMarketplaceMessageUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SendPassengerMarketplaceMessageUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.UnsubscribeMarketplaceChatUseCase

sealed class MarketplaceChatUiEvent {
    data class ShowSnackbar(val message: String) : MarketplaceChatUiEvent()
}

class MarketplaceChatViewModel(
    private val openPassengerMarketplaceConversationUseCase: OpenPassengerMarketplaceConversationUseCase,
    private val getOwnerMarketplaceMessagesUseCase: GetOwnerMarketplaceMessagesUseCase,
    private val sendPassengerMarketplaceMessageUseCase: SendPassengerMarketplaceMessageUseCase,
    private val sendOwnerMarketplaceMessageUseCase: SendOwnerMarketplaceMessageUseCase,
    private val subscribeMarketplaceChatUseCase: SubscribeMarketplaceChatUseCase,
    private val unsubscribeMarketplaceChatUseCase: UnsubscribeMarketplaceChatUseCase,
    private val observeMarketplaceChatMessagesUseCase: ObserveMarketplaceChatMessagesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketplaceChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<MarketplaceChatUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<MarketplaceChatUiEvent> = _uiEvents.asSharedFlow()

    private var realtimeJob: Job? = null
    private var nextTemporaryMessageId = -1L

    fun openPassengerListing(listingPublicId: String) {
        if (listingPublicId.isBlank()) return

        val current = _uiState.value
        if (current.listingPublicId == listingPublicId && current.threadId != null) {
            subscribe(MarketplaceChatParticipantRole.Passenger)
            return
        }

        _uiState.update {
            MarketplaceChatUiState(
                listingPublicId = listingPublicId,
                draft = it.draft.takeIf { current.listingPublicId == listingPublicId }.orEmpty(),
                isLoading = true,
            )
        }
        subscribe(MarketplaceChatParticipantRole.Passenger)

        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                openPassengerMarketplaceConversationUseCase(listingPublicId)
            }
            result
                .onSuccess { conversation ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            threadId = conversation.threadId,
                            listingPublicId = conversation.listingPublicId,
                            listing = conversation.listing,
                            messages = conversation.messages.sortedBy { message -> message.id },
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Unable to open conversation."
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error)
                    }
                    _uiEvents.tryEmit(MarketplaceChatUiEvent.ShowSnackbar(error))
                }
        }
    }

    fun openOwnerThread(
        threadId: Long,
        title: String,
        subtitle: String,
    ) {
        val current = _uiState.value
        if (current.ownerThreadId == threadId && current.threadId != null) {
            subscribe(MarketplaceChatParticipantRole.Owner)
            return
        }

        _uiState.update {
            MarketplaceChatUiState(
                ownerThreadId = threadId,
                threadId = threadId,
                title = title,
                subtitle = subtitle,
                draft = it.draft.takeIf { current.ownerThreadId == threadId }.orEmpty(),
                isLoading = true,
            )
        }
        subscribe(MarketplaceChatParticipantRole.Owner)

        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                getOwnerMarketplaceMessagesUseCase(threadId)
            }
            result
                .onSuccess { thread ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ownerThreadId = thread.threadId ?: threadId,
                            threadId = thread.threadId ?: threadId,
                            listingPublicId = thread.listingPublicId.orEmpty(),
                            messages = thread.messages.sortedBy { message -> message.id },
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Unable to open conversation."
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error)
                    }
                    _uiEvents.tryEmit(MarketplaceChatUiEvent.ShowSnackbar(error))
                }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update {
            it.copy(
                draft = value.take(MaxMarketplaceMessageLength),
                errorMessage = null,
            )
        }
    }

    fun send() {
        val state = _uiState.value
        val message = state.draft.trim()
        if (message.isBlank() || state.isSending) return
        if (state.ownerThreadId == null && state.listingPublicId.isBlank()) return

        val temporaryMessage = MarketplaceChatMessage(
            id = nextTemporaryMessageId--,
            threadId = state.threadId,
            listingPublicId = state.listingPublicId,
            senderUserId = 0,
            senderName = "You",
            message = message,
            sentAt = null,
            isMine = true,
        )

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    draft = "",
                    errorMessage = null,
                    messages = it.messages.mergeById(temporaryMessage),
                )
            }

            val result = withContext(ioDispatcher) {
                state.ownerThreadId?.let { threadId ->
                    sendOwnerMarketplaceMessageUseCase(
                        SendOwnerMarketplaceChatMessagePayload(
                            threadId = threadId,
                            message = message,
                        ),
                    )
                } ?: sendPassengerMarketplaceMessageUseCase(
                    SendMarketplaceChatMessagePayload(
                        listingPublicId = state.listingPublicId,
                        message = message,
                    ),
                )
            }
            result
                .onSuccess { sentMessage ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            threadId = sentMessage.threadId ?: it.threadId,
                            messages = it.messages
                                .filterNot { item -> item.id == temporaryMessage.id }
                                .mergeById(sentMessage),
                        )
                    }
                }
                .onFailure { throwable ->
                    val error = throwable.message ?: "Message failed to send."
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            draft = message,
                            messages = it.messages.filterNot { item -> item.id == temporaryMessage.id },
                            errorMessage = error,
                        )
                    }
                    _uiEvents.tryEmit(MarketplaceChatUiEvent.ShowSnackbar(error))
                }
        }
    }

    fun close() {
        realtimeJob?.cancel()
        realtimeJob = null
        unsubscribeMarketplaceChatUseCase()
    }

    private fun subscribe(role: MarketplaceChatParticipantRole) {
        subscribeMarketplaceChatUseCase(role)
        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            observeMarketplaceChatMessagesUseCase().collectLatest { message ->
                val state = _uiState.value
                val matchesListing = message.listingPublicId == state.listingPublicId
                val matchesThread = state.threadId != null && message.threadId == state.threadId
                if (!matchesListing && !matchesThread) return@collectLatest
                _uiState.update {
                    it.copy(
                        threadId = message.threadId ?: it.threadId,
                        messages = it.messages.mergeById(message),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        close()
        super.onCleared()
    }
}

private const val MaxMarketplaceMessageLength = 500

private fun List<MarketplaceChatMessage>.mergeById(message: MarketplaceChatMessage): List<MarketplaceChatMessage> {
    return filterNot { it.id == message.id } + message
}
