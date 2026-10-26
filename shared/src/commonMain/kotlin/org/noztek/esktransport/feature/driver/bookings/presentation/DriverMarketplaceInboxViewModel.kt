package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatParticipantRole
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.ObserveMarketplaceChatMessagesUseCase
import org.noztek.esktransport.feature.common.marketplace_chat.domain.usecase.SubscribeMarketplaceChatUseCase
import org.noztek.esktransport.feature.driver.bookings.domain.repository.DriverMarketplaceInquiriesRepository

class DriverMarketplaceInboxViewModel(
    private val repository: DriverMarketplaceInquiriesRepository,
    private val subscribeMarketplaceChatUseCase: SubscribeMarketplaceChatUseCase,
    private val observeMarketplaceChatMessagesUseCase: ObserveMarketplaceChatMessagesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _badgeCount = MutableStateFlow(0)
    val badgeCount = _badgeCount.asStateFlow()
    private var realtimeJob: Job? = null

    init {
        subscribe()
        load()
    }

    private fun subscribe() {
        subscribeMarketplaceChatUseCase(MarketplaceChatParticipantRole.Owner)
        if (realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
            observeMarketplaceChatMessagesUseCase().collectLatest { load() }
        }
    }

    private fun load() {
        viewModelScope.launch {
            withContext(ioDispatcher) { repository.getInbox() }
                .onSuccess { inbox -> _badgeCount.value = inbox.badgeCount }
        }
    }
}
