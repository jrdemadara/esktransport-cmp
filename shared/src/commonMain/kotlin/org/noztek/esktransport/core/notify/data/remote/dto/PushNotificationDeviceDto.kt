package org.noztek.esktransport.core.notify.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PushDeviceRegistrationRequestDto(
    val provider: String = "fcm",
    val platform: String,
    val token: String,
    @SerialName("device_id")
    val deviceId: String? = null,
    @SerialName("device_name")
    val deviceName: String? = null,
    @SerialName("app_version")
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
    val metadata: JsonObject? = null,
)

@Serializable
data class PushDeviceDeleteRequestDto(
    val token: String,
)
