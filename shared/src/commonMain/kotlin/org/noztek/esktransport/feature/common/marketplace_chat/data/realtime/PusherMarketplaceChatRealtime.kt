package org.noztek.esktransport.feature.common.marketplace_chat.data.realtime

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.RealtimeClient
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.feature.common.marketplace_chat.domain.model.MarketplaceChatMessage

class PusherMarketplaceChatRealtime(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val sessionManager: SessionManager,
    ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : MarketplaceChatRealtime {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val messages = MutableSharedFlow<MarketplaceChatMessage>(extraBufferCapacity = 64)
    private val subscribedChannels = mutableSetOf<String>()

    override fun subscribePassenger() {
        scope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            val channel = channelNamer.passengerPrivateChannel(userId)
            subscribe(channel)
        }
    }

    override fun subscribeOwner() {
        scope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            val channel = channelNamer.driverPrivateChannel(userId)
            subscribe(channel)
        }
    }

    override fun unsubscribe() {
        // This event shares the passenger private channel with booking updates.
        // Keep the channel active for other realtime listeners.
    }

    override fun messages() = messages.asSharedFlow()

    private fun subscribe(channel: String) {
        if (channel in subscribedChannels) return

        realtimeClient.subscribePrivateChannel(channel, "vehicle_marketplace.chat_message_created") { _, payload ->
            parseMessage(payload)?.let { messages.tryEmit(it) }
        }
        subscribedChannels += channel
    }

    private fun parseMessage(payload: String): MarketplaceChatMessage? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val messageId = root.long("message_id") ?: root.long("id") ?: return null
            val senderUserId = root.long("sender_user_id") ?: return null
            val currentUserId = sessionManager.userId.value
            MarketplaceChatMessage(
                id = messageId,
                threadId = root.long("thread_id"),
                listingPublicId = root.string("listing_public_id"),
                senderUserId = senderUserId,
                senderName = root.string("sender_name").orEmpty(),
                message = root.string("message").orEmpty(),
                sentAt = root.string("sent_at"),
                isMine = currentUserId != null && senderUserId == currentUserId,
            )
        }.getOrNull()
    }
}

private fun JsonObject.unwrapRealtimeData(json: Json): JsonObject {
    val dataNode = this["data"] ?: return this
    val objectNode = dataNode as? JsonObject
    if (objectNode != null) return objectNode
    val raw = dataNode.jsonPrimitive.contentOrNull ?: return this
    return runCatching { json.parseToJsonElement(raw).jsonObject }.getOrNull() ?: this
}

private fun JsonObject.string(key: String): String? = this[key]
    ?.jsonPrimitive
    ?.content
    ?.takeIf { it.isNotBlank() && it != "null" }

private fun JsonObject.long(key: String): Long? = string(key)?.toLongOrNull()
