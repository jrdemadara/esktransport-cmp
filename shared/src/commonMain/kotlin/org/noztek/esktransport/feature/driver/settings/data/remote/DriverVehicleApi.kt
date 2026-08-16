package org.noztek.esktransport.feature.driver.settings.data.remote

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
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverVehicleResponseDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverVehicleRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverVehicleServicesRequestDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverVehicleTypesResponseDto
import org.noztek.esktransport.feature.driver.settings.data.remote.dto.DriverVehiclesResponseDto
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocumentUploadPayload

class DriverVehicleApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getVehicleTypes(): DriverVehicleTypesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/lookups/vehicle-types").body()
    }

    suspend fun getVehicles(): DriverVehiclesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles").body()
    }

    suspend fun getVehicle(vehiclePublicId: String): DriverVehicleResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId").body()
    }

    suspend fun getVehiclePhoto(vehiclePublicId: String): ByteArray? {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId/photo").body()
        } catch (exception: ClientRequestException) {
            if (exception.response.status == HttpStatusCode.NotFound) {
                null
            } else {
                throw exception
            }
        }
    }

    suspend fun addVehicle(request: DriverVehicleRequestDto): DriverVehicleResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateVehicle(vehiclePublicId: String, request: DriverVehicleRequestDto): DriverVehicleResponseDto {
        return client.patch("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateVehicleServices(
        vehiclePublicId: String,
        request: DriverVehicleServicesRequestDto,
    ): DriverVehicleResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId/services") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun uploadVehicleDocument(
        vehiclePublicId: String,
        upload: DriverVehicleDocumentUploadPayload,
    ): DriverVehicleResponseDto {
        return client.submitFormWithBinaryData(
            url = "${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId/documents",
            formData = formData {
                append("type", upload.type.apiValue)
                upload.expiresAt?.let { append("expires_at", it) }
                append(
                    key = "document",
                    value = upload.bytes,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"document\"; filename=\"${upload.fileName}\"",
                        )
                        append(HttpHeaders.ContentType, upload.mimeType)
                    },
                )
            },
        ).body()
    }

    suspend fun activateRideVehicle(vehiclePublicId: String): DriverVehicleResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/vehicles/$vehiclePublicId/activate-ride").body()
    }
}
