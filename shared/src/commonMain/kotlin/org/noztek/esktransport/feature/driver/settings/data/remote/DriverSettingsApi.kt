package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class DriverSettingsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
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
