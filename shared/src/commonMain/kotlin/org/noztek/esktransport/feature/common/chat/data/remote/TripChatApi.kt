package org.noztek.esktransport.feature.common.chat.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.common.chat.data.remote.dto.SendTripChatMessageRequestDto
import org.noztek.esktransport.feature.common.chat.data.remote.dto.TripChatMessageResponseDto
import org.noztek.esktransport.feature.common.chat.data.remote.dto.TripChatMessagesResponseDto
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole

class TripChatApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getMessages(
        bookingPublicId: String,
        role: TripChatParticipantRole,
    ): TripChatMessagesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/${role.apiBasePath()}/bookings/$bookingPublicId/messages").body()
    }

    suspend fun sendMessage(
        bookingPublicId: String,
        role: TripChatParticipantRole,
        request: SendTripChatMessageRequestDto,
    ): TripChatMessageResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/${role.apiBasePath()}/bookings/$bookingPublicId/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

private fun TripChatParticipantRole.apiBasePath(): String {
    return when (this) {
        TripChatParticipantRole.Passenger -> "api/passenger"
        TripChatParticipantRole.Driver -> "api/v1/rider"
    }
}
