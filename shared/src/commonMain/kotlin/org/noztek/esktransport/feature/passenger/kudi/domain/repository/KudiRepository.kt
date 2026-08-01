package org.noztek.esktransport.feature.passenger.kudi.domain.repository

import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversation
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiMessageResult

interface KudiRepository {
    suspend fun getCurrentSession(): Result<KudiConversation>

    suspend fun createSession(): Result<KudiConversation>

    suspend fun sendMessage(sessionPublicId: String, message: String): Result<KudiMessageResult>
}
