package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverLocationSharingRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverLocationSharingResponseDto

class DriverLocationSharingApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val endpoint = "${baseUrl.trimEnd('/')}/api/v1/rider/location-sharing"

    suspend fun getSettings(): DriverLocationSharingResponseDto {
        return client.get(endpoint).body()
    }

    suspend fun updateSettings(request: DriverLocationSharingRequestDto): DriverLocationSharingResponseDto {
        return client.patch(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
