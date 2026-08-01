package org.noztek.esktransport.feature.passenger.kudi.presentation

import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiChatMessage

data class KudiUiState(
    val sessionPublicId: String? = null,
    val messages: List<KudiChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)
