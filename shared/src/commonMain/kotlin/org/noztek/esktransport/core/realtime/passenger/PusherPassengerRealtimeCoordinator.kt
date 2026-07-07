package org.noztek.esktransport.core.realtime.passenger

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
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.RealtimeClient
import org.noztek.esktransport.core.realtime.model.PassengerBookingAcceptedEvent
import org.noztek.esktransport.core.realtime.model.PassengerBookingCancelledEvent
import org.noztek.esktransport.core.realtime.model.PassengerBookingOfferExpiredEvent
import org.noztek.esktransport.core.realtime.model.PassengerBookingSearchExpiredEvent
import org.noztek.esktransport.core.realtime.model.PassengerDriverAssignedEvent
import org.noztek.esktransport.core.realtime.model.PassengerTripCompletedEvent
import org.noztek.esktransport.core.realtime.model.PassengerTripLocationUpdatedEvent
import org.noztek.esktransport.core.session.SessionManager

class PusherPassengerRealtimeCoordinator(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val sessionManager: SessionManager,
    ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : PassengerRealtimeCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val driverAssigned = MutableSharedFlow<PassengerDriverAssignedEvent>(extraBufferCapacity = 32)
    private val bookingAccepted = MutableSharedFlow<PassengerBookingAcceptedEvent>(extraBufferCapacity = 32)
    private val bookingCancelled = MutableSharedFlow<PassengerBookingCancelledEvent>(extraBufferCapacity = 32)
    private val offerExpired = MutableSharedFlow<PassengerBookingOfferExpiredEvent>(extraBufferCapacity = 32)
    private val searchExpired = MutableSharedFlow<PassengerBookingSearchExpiredEvent>(extraBufferCapacity = 32)
    private val tripLocationUpdated = MutableSharedFlow<PassengerTripLocationUpdatedEvent>(extraBufferCapacity = 64)
    private val tripCompleted = MutableSharedFlow<PassengerTripCompletedEvent>(extraBufferCapacity = 16)
    private var driverAssignedChannel: String? = null

    override fun subscribePassengerDriverAssigned() {
        scope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            val roles = sessionManager.userRoles.first()
            val role = sessionManager.userRole.first()
            if (role !in setOf("customer", "passenger") && roles.none { it == "customer" || it == "passenger" }) return@launch

            val channel = channelNamer.passengerPrivateChannel(userId)
            if (driverAssignedChannel == channel) return@launch

            driverAssignedChannel?.let(realtimeClient::unsubscribe)
            realtimeClient.subscribePrivateChannel(channel, "booking.driver_assigned") { _, payload ->
                parseDriverAssigned(payload)?.let { driverAssigned.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "booking.accepted") { _, payload ->
                println("Passenger realtime raw booking.accepted payload: $payload")
                parseBookingAccepted(payload)?.let { bookingAccepted.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "booking.cancelled") { _, payload ->
                parseBookingCancelled(payload)?.let { bookingCancelled.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "booking.offer_expired") { _, payload ->
                parseOfferExpired(payload)?.let { offerExpired.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "booking.search_expired") { _, payload ->
                parseSearchExpired(payload)?.let { searchExpired.tryEmit(it) }
            }
            realtimeClient.subscribePrivateChannel(channel, "trip.driver_location_updated") { _, payload ->
                println("Passenger realtime raw trip.driver_location_updated payload: $payload")
                val event = parseTripLocationUpdated(payload)
                if (event == null) {
                    println("Passenger realtime dropped trip.driver_location_updated payload.")
                } else {
                    tripLocationUpdated.tryEmit(event)
                }
            }
            realtimeClient.subscribePrivateChannel(channel, "trip.completed") { _, payload ->
                parseTripCompleted(payload)?.let { tripCompleted.tryEmit(it) }
            }
            driverAssignedChannel = channel
        }
    }

    override fun unsubscribePassengerDriverAssigned() {
        driverAssignedChannel?.let(realtimeClient::unsubscribe)
        driverAssignedChannel = null
    }

    override fun passengerDriverAssigned() = driverAssigned.asSharedFlow()
    override fun passengerBookingAccepted() = bookingAccepted.asSharedFlow()
    override fun passengerBookingCancelled() = bookingCancelled.asSharedFlow()
    override fun passengerBookingOfferExpired() = offerExpired.asSharedFlow()
    override fun passengerBookingSearchExpired() = searchExpired.asSharedFlow()
    override fun passengerTripLocationUpdated() = tripLocationUpdated.asSharedFlow()
    override fun passengerTripCompleted() = tripCompleted.asSharedFlow()

    private fun parseDriverAssigned(payload: String): PassengerDriverAssignedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            val riderUserId = root.long("rider_user_id") ?: return null
            PassengerDriverAssignedEvent(
                bookingPublicId = bookingPublicId,
                riderUserId = riderUserId,
                driverPublicId = root.string("driver_public_id"),
                vehicleTypeCode = root.string("vehicle_type_code"),
                vehicleLabel = root.string("vehicle_label"),
                vehiclePlate = root.string("vehicle_plate"),
                passengerCapacity = root.int("passenger_capacity"),
                finalFare = root.double("final_fare"),
            )
        }.getOrNull()
    }

    private fun parseBookingAccepted(payload: String): PassengerBookingAcceptedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            println("Passenger realtime parsed booking.accepted booking_public_id=$bookingPublicId")
            PassengerBookingAcceptedEvent(bookingPublicId = bookingPublicId)
        }.onFailure {
            println("Passenger realtime failed parsing booking.accepted: ${it.message}")
        }.getOrNull()
    }

    private fun parseOfferExpired(payload: String): PassengerBookingOfferExpiredEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            PassengerBookingOfferExpiredEvent(
                bookingPublicId = bookingPublicId,
                riderUserId = root.long("rider_user_id"),
            )
        }.getOrNull()
    }

    private fun parseBookingCancelled(payload: String): PassengerBookingCancelledEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            PassengerBookingCancelledEvent(
                bookingPublicId = bookingPublicId,
                riderUserId = root.long("rider_user_id"),
                cancelledBy = root.string("cancelled_by"),
            )
        }.getOrNull()
    }

    private fun parseSearchExpired(payload: String): PassengerBookingSearchExpiredEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            PassengerBookingSearchExpiredEvent(bookingPublicId = bookingPublicId)
        }.getOrNull()
    }

    private fun parseTripLocationUpdated(payload: String): PassengerTripLocationUpdatedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            val lat = root.double("lat") ?: return null
            val lng = root.double("lng") ?: return null
            PassengerTripLocationUpdatedEvent(
                bookingPublicId = bookingPublicId,
                latitude = lat,
                longitude = lng,
                bearing = root.double("bearing"),
                speedKph = root.double("speed_kph"),
                accuracyM = root.double("accuracy_m"),
                recordedAt = root.string("recorded_at"),
                phase = root.string("phase"),
            )
        }.onFailure {
            println("Passenger realtime failed parsing trip.driver_location_updated: ${it.message}")
        }.getOrNull()
    }

    private fun parseTripCompleted(payload: String): PassengerTripCompletedEvent? {
        return runCatching {
            val parsed = json.parseToJsonElement(payload).jsonObject
            val root = parsed.unwrapRealtimeData(json)
            val bookingPublicId = root.string("booking_public_id") ?: return null
            PassengerTripCompletedEvent(
                bookingPublicId = bookingPublicId,
                riderUserId = root.long("rider_user_id"),
                finalFare = root.double("final_fare"),
                currency = root.string("currency"),
                completedAt = root.string("completed_at"),
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

private fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

private fun JsonObject.long(key: String): Long? = string(key)?.toLongOrNull()
