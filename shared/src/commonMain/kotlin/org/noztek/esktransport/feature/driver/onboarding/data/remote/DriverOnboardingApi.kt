package org.noztek.esktransport.feature.driver.onboarding.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverOnboardingMutationResponseDto
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverOnboardingResponseDto
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverServiceZoneSelectionRequestDto
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverServiceZonesResponseDto
import org.noztek.esktransport.feature.driver.onboarding.data.remote.dto.DriverVehicleSetupRequestDto
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverIdentityVerificationPayload
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentUpload

class DriverOnboardingApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun getStatus(): DriverOnboardingResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/onboarding").body()
    }

    suspend fun saveVehicle(request: DriverVehicleSetupRequestDto): DriverOnboardingMutationResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/vehicle") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getServiceZones(): DriverServiceZonesResponseDto {
        return client.get("${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/service-zones").body()
    }

    suspend fun submitServiceZones(request: DriverServiceZoneSelectionRequestDto): DriverOnboardingMutationResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/service-zone") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun uploadDocument(upload: DriverOnboardingDocumentUpload): DriverOnboardingMutationResponseDto {
        return client.submitFormWithBinaryData(
            url = "${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/documents",
            formData = formData {
                append("type", upload.type.apiValue)
                upload.licenseNo?.let { append("license_no", it) }
                upload.licenseExpiry?.let { append("license_expiry", it) }
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

    suspend fun submitIdentityVerification(payload: DriverIdentityVerificationPayload): DriverOnboardingMutationResponseDto {
        return client.submitFormWithBinaryData(
            url = "${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/identity",
            formData = formData {
                append("license_no", payload.licenseNo)
                append("license_expiry", payload.licenseExpiry)
                appendIdentityFile("license_front", payload.licenseFront)
                appendIdentityFile("license_back", payload.licenseBack)
                appendIdentityFile("selfie", payload.selfie)
            },
        ).body()
    }

    suspend fun submitForReview(): DriverOnboardingMutationResponseDto {
        return client.post("${baseUrl.trimEnd('/')}/api/v1/rider/onboarding/submit").body()
    }
}

private fun FormBuilder.appendIdentityFile(
    key: String,
    upload: DriverOnboardingDocumentUpload,
) {
    append(
        key = key,
        value = upload.bytes,
        headers = Headers.build {
            append(
                HttpHeaders.ContentDisposition,
                "form-data; name=\"$key\"; filename=\"${upload.fileName}\"",
            )
            append(HttpHeaders.ContentType, upload.mimeType)
        },
    )
}
