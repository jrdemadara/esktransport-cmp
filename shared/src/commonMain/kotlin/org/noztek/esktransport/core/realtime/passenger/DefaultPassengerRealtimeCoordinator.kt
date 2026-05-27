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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noztek.esktransport.core.realtime.RealtimeChannelNamer
import org.noztek.esktransport.core.realtime.RealtimeClient
import org.noztek.esktransport.core.realtime.model.PassengerDriverAssignedEvent
import org.noztek.esktransport.core.session.SessionManager

class DefaultPassengerRealtimeCoordinator(
    private val realtimeClient: RealtimeClient,
    private val channelNamer: RealtimeChannelNamer,
    private val sessionManager: SessionManager,
    ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) : PassengerRealtimeCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val driverAssigned = MutableSharedFlow<PassengerDriverAssignedEvent>(extraBufferCapacity = 32)
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
            driverAssignedChannel = channel
        }
    }

    override fun unsubscribePassengerDriverAssigned() {
        driverAssignedChannel?.let(realtimeClient::unsubscribe)
        driverAssignedChannel = null
    }

    override fun passengerDriverAssigned() = driverAssigned.asSharedFlow()

    private fun parseDriverAssigned(payload: String): PassengerDriverAssignedEvent? {
        return runCatching {
            val root = json.parseToJsonElement(payload).jsonObject
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
}

private fun JsonObject.string(key: String): String? = this[key]
    ?.jsonPrimitive
    ?.content
    ?.takeIf { it.isNotBlank() && it != "null" }

private fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

private fun JsonObject.long(key: String): Long? = string(key)?.toLongOrNull()
