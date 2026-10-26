package org.noztek.esktransport.feature.common.notification.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import org.noztek.esktransport.feature.common.notification.data.remote.dto.NotificationsResponseDto

class NotificationApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val endpoint: String
        get() = "${baseUrl.trimEnd('/')}/api/v1/notifications"

    suspend fun getNotifications(): NotificationsResponseDto {
        return client.get(endpoint).body()
    }

    suspend fun markAllRead() {
        client.post("$endpoint/read-all")
    }
}
