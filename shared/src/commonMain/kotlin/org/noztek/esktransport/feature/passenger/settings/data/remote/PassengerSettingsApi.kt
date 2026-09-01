package org.noztek.esktransport.feature.passenger.settings.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.PassengerAccountResponseDto
import org.noztek.esktransport.feature.passenger.settings.data.remote.dto.UpdatePassengerAccountRequestDto
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerProfilePhotoUpload

class PassengerSettingsApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getAccount(): PassengerAccountResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/passenger/me").body()
    }

    suspend fun updateAccount(request: UpdatePassengerAccountRequestDto): PassengerAccountResponseDto {
        return client.patch("${baseUrl.trimEnd('/')}/api/passenger/account") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getProfilePhoto(): ByteArray? {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/passenger/profile-photo").body()
        } catch (exception: ClientRequestException) {
            if (exception.response.status == HttpStatusCode.NotFound) {
                null
            } else {
                throw exception
            }
        }
    }

    suspend fun uploadProfilePhoto(upload: PassengerProfilePhotoUpload): PassengerAccountResponseDto {
        return client.submitFormWithBinaryData(
            url = "${baseUrl.trimEnd('/')}/api/passenger/profile-photo",
            formData = formData {
                append(
                    key = "photo",
                    value = upload.bytes,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"photo\"; filename=\"${upload.fileName}\"",
                        )
                        append(HttpHeaders.ContentType, upload.mimeType)
                    },
                )
            },
        ).body()
    }
}
