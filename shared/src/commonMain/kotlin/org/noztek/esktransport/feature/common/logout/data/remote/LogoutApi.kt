package org.noztek.esktransport.feature.common.logout.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LogoutApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun logout() {
        client.post("${baseUrl.trimEnd('/')}/api/v1/auth/logout") {
            contentType(ContentType.Application.Json)
        }
    }
}
