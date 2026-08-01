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
