package org.noztek.esktransport.feature.passenger.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.SavedPlaceMessageResponseDto
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.SavedPlaceRequestDto
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.SavedPlaceResponseDto
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.SavedPlacesResponseDto

class SavedPlacesApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getSavedPlaces(): SavedPlacesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/saved-places").body()
    }

    suspend fun createSavedPlace(request: SavedPlaceRequestDto): SavedPlaceResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/passenger/saved-places") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateSavedPlace(id: Long, request: SavedPlaceRequestDto): SavedPlaceResponseDto {
        return client.patch("${baseUrl.trimEnd('/')}/api/passenger/saved-places/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteSavedPlace(id: Long): SavedPlaceMessageResponseDto {
        return client.delete("${baseUrl.trimEnd('/')}/api/passenger/saved-places/$id").body()
    }
}
