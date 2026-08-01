package org.noztek.esktransport.feature.passenger.kudi.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.passenger.kudi.data.remote.KudiApi
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiConversation
import org.noztek.esktransport.feature.passenger.kudi.domain.model.KudiMessageResult
import org.noztek.esktransport.feature.passenger.kudi.domain.repository.KudiRepository

class KudiRepositoryImpl(
    private val api: KudiApi,
) : KudiRepository {
    override suspend fun getCurrentSession(): Result<KudiConversation> {
        return try {
            Result.success(api.getCurrentSession().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Unable to load Kudi.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun createSession(): Result<KudiConversation> {
        return try {
            Result.success(api.createSession().data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Unable to start Kudi.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun sendMessage(sessionPublicId: String, message: String): Result<KudiMessageResult> {
        return try {
            Result.success(api.sendMessage(sessionPublicId = sessionPublicId, message = message).data.toDomain())
        } catch (throwable: Throwable) {
            val error = ApiErrorParser.parse(throwable, "Unable to send message.")
            Result.failure(IllegalStateException(error))
        }
    }
}
