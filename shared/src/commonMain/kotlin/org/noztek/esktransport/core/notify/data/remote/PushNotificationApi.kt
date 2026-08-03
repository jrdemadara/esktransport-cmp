package org.noztek.esktransport.core.notify.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import org.noztek.esktransport.core.notify.data.remote.dto.PushDeviceDeleteRequestDto
import org.noztek.esktransport.core.notify.data.remote.dto.PushDeviceRegistrationRequestDto

class PushNotificationApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val endpoint: String
        get() = "${baseUrl.trimEnd('/')}/api/v1/notifications/devices"

    suspend fun registerDevice(request: PushDeviceRegistrationRequestDto) {
        client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun unregisterDevice(request: PushDeviceDeleteRequestDto) {
        client.request(endpoint) {
            method = HttpMethod.Delete
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
