package org.noztek.esktransport.feature.common.chat.data.impl

import org.noztek.esktransport.core.network.ApiErrorParser
import org.noztek.esktransport.feature.common.chat.data.remote.TripChatApi
import org.noztek.esktransport.feature.common.chat.data.remote.dto.SendTripChatMessageRequestDto
import org.noztek.esktransport.feature.common.chat.domain.model.SendTripChatMessagePayload
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatMessage
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole
import org.noztek.esktransport.feature.common.chat.domain.repository.TripChatRepository

class TripChatRepositoryImpl(
    private val api: TripChatApi,
) : TripChatRepository {
    override suspend fun getMessages(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ): Result<List<TripChatMessage>> {
        return try {
            Result.success(api.getMessages(bookingPublicId, role).data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to load chat.")
            Result.failure(IllegalStateException(message))
        }
    }

    override suspend fun sendMessage(payload: SendTripChatMessagePayload): Result<TripChatMessage> {
        return try {
            val response = api.sendMessage(
                bookingPublicId = payload.bookingPublicId,
                role = payload.role,
                request = SendTripChatMessageRequestDto(message = payload.message),
            )
            Result.success(response.data.toDomain())
        } catch (throwable: Throwable) {
            val message = ApiErrorParser.parse(throwable, "Failed to send message.")
            Result.failure(IllegalStateException(message))
        }
    }
}
