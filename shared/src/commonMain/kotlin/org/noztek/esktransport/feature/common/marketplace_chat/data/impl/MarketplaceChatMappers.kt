package org.noztek.esktransport.feature.common.marketplace_chat.data.impl

import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceChatMessageDto
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceChatListingDto
import org.noztek.esktransport.feature.common.marketplace_chat.data.remote.dto.MarketplaceConversationDto
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatListing
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceConversation

fun MarketplaceConversationDto.toDomain(): MarketplaceConversation {
    return MarketplaceConversation(
        threadId = threadId,
        listingPublicId = listingPublicId,
        listing = listing.toDomain(),
        messages = messages.map { it.toDomain(listingPublicId) },
    )
}

fun MarketplaceChatListingDto.toDomain(): MarketplaceChatListing {
    return MarketplaceChatListing(
        publicId = publicId,
        serviceType = serviceType,
        title = title,
        ownerName = owner.name,
    )
}

fun MarketplaceChatMessageDto.toDomain(listingPublicId: String?): MarketplaceChatMessage {
    return MarketplaceChatMessage(
        id = id,
        threadId = threadId,
        listingPublicId = listingPublicId,
        senderUserId = senderUserId,
        senderName = senderName,
        message = message,
        sentAt = sentAt,
        isMine = isMine,
    )
}
