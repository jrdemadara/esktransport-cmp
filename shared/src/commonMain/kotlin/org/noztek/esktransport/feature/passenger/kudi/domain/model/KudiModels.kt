package org.noztek.esktransport.feature.passenger.kudi.domain.model

data class KudiConversation(
    val session: KudiSession?,
    val messages: List<KudiChatMessage>,
    val state: KudiConversationState?,
)

data class KudiMessageResult(
    val conversation: KudiConversation,
    val assistant: KudiAssistant?,
)

data class KudiSession(
    val publicId: String,
    val status: String,
    val intent: String?,
)

data class KudiChatMessage(
    val id: String,
    val sender: KudiMessageSender,
    val message: String,
    val createdAt: String?,
)

enum class KudiMessageSender {
    User,
    Assistant,
    System,
}

data class KudiConversationState(
    val intent: String?,
    val allowed: Boolean?,
    val missingFields: List<String>,
    val lastAssistantMessage: String?,
)

data class KudiAssistant(
    val allowed: Boolean,
    val intent: String?,
    val reply: String,
    val missingFields: List<String>,
)
