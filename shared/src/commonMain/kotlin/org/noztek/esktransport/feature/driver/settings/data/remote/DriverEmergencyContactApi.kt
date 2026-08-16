package org.noztek.esktransport.feature.driver.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverEmergencyContactRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverEmergencyContactsResponseDto

class DriverEmergencyContactApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    private val endpoint = "${baseUrl.trimEnd('/')}/api/v1/rider/emergency-contacts"

    suspend fun getContacts(): DriverEmergencyContactsResponseDto {
        return client.get(endpoint).body()
    }

    suspend fun createContact(request: DriverEmergencyContactRequestDto): DriverEmergencyContactsResponseDto {
        return client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateContact(
        contactId: Long,
        request: DriverEmergencyContactRequestDto,
    ): DriverEmergencyContactsResponseDto {
        return client.patch("$endpoint/$contactId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteContact(contactId: Long): DriverEmergencyContactsResponseDto {
        return client.delete("$endpoint/$contactId").body()
    }
}
