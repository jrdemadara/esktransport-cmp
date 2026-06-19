package org.noztek.esktransport.core.realtime.driver

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.RealtimeClient
import org.noztek.esktransport.core.realtime.model.DriverOnboardingStatusChangedEvent

class PusherDriverOnboardingRealtime(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val json: Json,
) : DriverOnboardingRealtime {
    private val onboardingStatusChanged = MutableSharedFlow<DriverOnboardingStatusChangedEvent>(extraBufferCapacity = 32)
    private var onboardingChannel: String? = null

    override fun subscribeDriverOnboarding(driverId: Long) {
        val channel = channelNamer.driverOnboardingPrivateChannel(driverId)
        if (onboardingChannel == channel) return

        onboardingChannel?.let(realtimeClient::unsubscribe)
        realtimeClient.subscribePrivateChannel(channel, "onboarding.status_changed") { _, payload ->
            parseOnboardingStatusChanged(payload)?.let { onboardingStatusChanged.tryEmit(it) }
        }
        onboardingChannel = channel
    }

    override fun unsubscribeDriverOnboarding() {
        onboardingChannel?.let(realtimeClient::unsubscribe)
        onboardingChannel = null
    }

    override fun onboardingStatusChanged() = onboardingStatusChanged.asSharedFlow()

    private fun parseOnboardingStatusChanged(payload: String): DriverOnboardingStatusChangedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val driverId = root.long("driver_id") ?: return null

            DriverOnboardingStatusChangedEvent(
                driverId = driverId,
                driverUserId = root.long("driver_user_id"),
                step = root.string("step"),
                status = root.string("status"),
                message = root.string("message"),
                reviewedAt = root.string("reviewed_at"),
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
