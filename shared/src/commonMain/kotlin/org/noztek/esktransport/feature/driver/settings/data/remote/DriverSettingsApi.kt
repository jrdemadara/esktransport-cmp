package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverAccountResponseDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.UpdateDriverAccountRequestDto

class DriverSettingsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getAccount(): DriverAccountResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/me").body()
    }

    suspend fun updateAccount(request: UpdateDriverAccountRequestDto): DriverAccountResponseDto {
        return client.patch("${baseUrl.trimEnd('/')}/api/v1/rider/account") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getProfilePhoto(): ByteArray? {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/v1/rider/profile-photo").body()
        } catch (exception: ClientRequestException) {
            if (exception.response.status == HttpStatusCode.NotFound) {
                null
            } else {
                throw exception
            }
        }
    }
}
