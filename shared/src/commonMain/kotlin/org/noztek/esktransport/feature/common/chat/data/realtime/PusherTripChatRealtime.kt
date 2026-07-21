package org.noztek.esktransport.feature.common.chat.data.realtime

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
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole
import org.noztek.esktransport.feature.common.chat.domain.model.toTripChatParticipantRole

class PusherTripChatRealtime(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val sessionManager: SessionManager,
    ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : TripChatRealtime {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val messages = MutableSharedFlow<TripChatMessage>(extraBufferCapacity = 64)
    private var subscribedChannel: String? = null

    override fun subscribe(role: TripChatParticipantRole) {
        scope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            val channel = when (role) {
                TripChatParticipantRole.Passenger -> channelNamer.passengerPrivateChannel(userId)
                TripChatParticipantRole.Driver -> channelNamer.driverPrivateChannel(userId)
            }
            if (subscribedChannel == channel) return@launch

            realtimeClient.subscribePrivateChannel(channel, "trip.chat_message_created") { _, payload ->
                parseMessage(payload)?.let { messages.tryEmit(it) }
            }
            subscribedChannel = channel
        }
    }

    override fun unsubscribe() {
        // The trip chat event shares the same user private channel with trip status and booking events.
        // RealtimeClient currently unsubscribes the whole channel, so keep this as a no-op.
    }

    override fun messages() = messages.asSharedFlow()

    private fun parseMessage(payload: String): TripChatMessage? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val messageId = root.long("message_id") ?: root.long("id") ?: return null
            val bookingPublicId = root.string("booking_public_id") ?: return null
            val senderUserId = root.long("sender_user_id") ?: return null
            val currentUserId = sessionManager.userId.value
            TripChatMessage(
                id = messageId,
                bookingPublicId = bookingPublicId,
                senderUserId = senderUserId,
                senderRole = root.string("sender_role").orEmpty().toTripChatParticipantRole(),
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
