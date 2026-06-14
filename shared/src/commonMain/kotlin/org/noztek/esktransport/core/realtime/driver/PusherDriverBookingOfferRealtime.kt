package org.noztek.esktransport.core.realtime.driver

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.RealtimeClient
import org.noztek.esktransport.core.realtime.model.DriverBookingCancelledEvent
import org.noztek.esktransport.core.realtime.model.DriverBookingOfferedEvent
import org.noztek.esktransport.core.session.SessionManager

class PusherDriverBookingOfferRealtime(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val sessionManager: SessionManager,
    ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : DriverBookingOfferRealtime {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val bookingOffers = MutableSharedFlow<DriverBookingOfferedEvent>(extraBufferCapacity = 32)
    private val bookingCancelled = MutableSharedFlow<DriverBookingCancelledEvent>(extraBufferCapacity = 32)
    private var bookingOffersChannel: String? = null

    override fun subscribeDriverBookingOffers() {
        scope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            val roles = sessionManager.userRoles.first()
            val role = sessionManager.userRole.first()
            if (role != "driver" && !roles.contains("driver")) return@launch

            val channel = channelNamer.driverPrivateChannel(userId)
            if (bookingOffersChannel == channel) return@launch

            bookingOffersChannel?.let(realtimeClient::unsubscribe)
            realtimeClient.subscribePrivateChannel(channel, "booking.offered") { _, payload ->
                parseBookingOffer(payload)?.let { bookingOffers.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "booking.cancelled") { _, payload ->
                parseBookingCancelled(payload)?.let { bookingCancelled.tryEmit(it) }
            }
            bookingOffersChannel = channel
        }
    }

    override fun unsubscribeDriverBookingOffers() {
        bookingOffersChannel?.let(realtimeClient::unsubscribe)
        bookingOffersChannel = null
    }

    override fun driverBookingOffers() = bookingOffers.asSharedFlow()
    override fun driverBookingCancelled() = bookingCancelled.asSharedFlow()

    private fun parseBookingOffer(payload: String): DriverBookingOfferedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            DriverBookingOfferedEvent(
                bookingPublicId = bookingPublicId,
                passengerUserId = root.long("passenger_user_id"),
                passengerName = root.string("passenger_name"),
                pickupLabel = root.objectValue("pickup")?.string("label")
                    ?: root.string("pickup_label")
                    ?: "N/A",
                pickupLat = root.objectValue("pickup")?.double("lat") ?: root.double("pickup_lat"),
                pickupLng = root.objectValue("pickup")?.double("lng") ?: root.double("pickup_lng"),
                destinationLabel = root.objectValue("destination")?.string("label")
                    ?: root.string("destination_label")
                    ?: "N/A",
                destinationLat = root.objectValue("destination")?.double("lat") ?: root.double("destination_lat"),
                destinationLng = root.objectValue("destination")?.double("lng") ?: root.double("destination_lng"),
                finalFare = root.double("final_fare"),
            )
        }.getOrNull()
    }

    private fun parseBookingCancelled(payload: String): DriverBookingCancelledEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            DriverBookingCancelledEvent(
                bookingPublicId = bookingPublicId,
                passengerUserId = root.long("passenger_user_id"),
                cancelledBy = root.string("cancelled_by"),
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

private fun JsonObject.objectValue(key: String): JsonObject? = this[key]?.jsonObject

private fun JsonObject.string(key: String): String? = this[key]
    ?.jsonPrimitive
    ?.content
    ?.takeIf { it.isNotBlank() && it != "null" }

private fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.long(key: String): Long? = string(key)?.toLongOrNull()
