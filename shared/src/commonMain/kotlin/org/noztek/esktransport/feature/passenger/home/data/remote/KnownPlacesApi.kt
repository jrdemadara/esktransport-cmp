package org.noztek.esktransport.feature.passenger.home.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.noztek.esktransport.feature.passenger.home.data.remote.dto.KnownPlacesResponseDto

class KnownPlacesApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getKnownPlaces(): KnownPlacesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/known-places").body()
    }
}
