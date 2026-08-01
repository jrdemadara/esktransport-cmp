package org.noztek.esktransport.feature.passenger.activity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.passenger.activity.data.remote.dto.PassengerActivityResponseDto

class PassengerActivityApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getActivity(): PassengerActivityResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/activity").body()
    }
}
