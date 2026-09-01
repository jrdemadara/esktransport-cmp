package org.noztek.esktransport.feature.common.marketplace_chat.presentation

import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatListing
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage

data class MarketplaceChatUiState(
    val listingPublicId: String = "",
    val ownerThreadId: Long? = null,
    val threadId: Long? = null,
    val listing: MarketplaceChatListing? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val messages: List<MarketplaceChatMessage> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)
