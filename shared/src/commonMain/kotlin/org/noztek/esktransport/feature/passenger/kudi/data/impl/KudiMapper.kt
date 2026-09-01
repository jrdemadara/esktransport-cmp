package org.noztek.esktransport.feature.passenger.kudi.data.impl

import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiAssistantDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiMessageDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiMessagePayloadDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiSessionDto
import org.noztek.esktransport.feature.passenger.kudi.data.remote.dto.KudiSessionPayloadDto
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiAssistant
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiChatMessage
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversation
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversationState
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiMessageResult
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiMessageSender
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiSession
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiVehicleSuggestion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun KudiSessionPayloadDto.toDomain(): KudiConversation {
    return KudiConversation(
        session = session?.toDomain(),
        messages = messages.map { it.toDomain() },
        state = null,
    )
}

fun KudiMessagePayloadDto.toDomain(): KudiMessageResult {
    return KudiMessageResult(
        conversation = KudiConversation(
            session = session?.toDomain(),
            messages = messages.map { it.toDomain() },
            state = null,
        ),
        assistant = assistant?.toDomain(),
    )
}

private fun KudiSessionDto.toDomain(): KudiSession {
    return KudiSession(
        publicId = publicId,
        status = status,
        intent = intent,
    )
}

private fun KudiMessageDto.toDomain(): KudiChatMessage {
    return KudiChatMessage(
        id = id?.toString() ?: "${sender}_${createdAt.orEmpty()}_${message.hashCode()}",
        sender = sender.toSender(),
        message = message,
        createdAt = createdAt,
        vehicleSuggestions = meta.vehicleSuggestions(),
    )
}

private fun KudiAssistantDto.toDomain(): KudiAssistant {
    return KudiAssistant(
        allowed = allowed,
        intent = intent,
        reply = reply,
        missingFields = missingFields,
    )
}

private fun String.toSender(): KudiMessageSender {
    return when (lowercase()) {
        "user" -> KudiMessageSender.User
        "assistant" -> KudiMessageSender.Assistant
        "system" -> KudiMessageSender.System
        else -> KudiMessageSender.Assistant
    }
}

private fun JsonElement?.vehicleSuggestions(): List<KudiVehicleSuggestion> {
    val meta = this as? JsonObject ?: return emptyList()
    val toolResults = meta["tool_results"] as? JsonArray ?: return emptyList()

    return toolResults
        .mapNotNull { it as? JsonObject }
        .filter { it.string("type") == "search_vehicle_listings" }
        .flatMap { result ->
            val listings = result["listings"] as? JsonArray ?: return@flatMap emptyList()
            listings.mapNotNull { listingElement -> (listingElement as? JsonObject)?.toVehicleSuggestion() }
        }
}

private fun JsonObject.toVehicleSuggestion(): KudiVehicleSuggestion? {
    val publicId = string("public_id") ?: return null

    return KudiVehicleSuggestion(
        publicId = publicId,
        serviceType = string("service_type").orEmpty(),
        title = string("title").orEmpty(),
        ownerName = string("owner_name").orEmpty(),
        vehicleTypeCode = string("vehicle_type_code").orEmpty(),
        vehicleTypeLabel = string("vehicle_type_label"),
        plate = string("plate").orEmpty(),
        make = string("make"),
        model = string("model"),
        year = int("year"),
        payloadKg = double("payload_kg"),
        volumeM3 = double("volume_m3"),
        passengerCapacity = int("passenger_capacity"),
        baseRate = double("base_rate"),
        rateUnit = string("rate_unit"),
        includedKm = double("included_km"),
        currency = string("currency") ?: "PHP",
        isBestMatch = boolean("is_best_match") ?: false,
    )
}

private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

private fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull

private fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull

private fun JsonObject.boolean(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
