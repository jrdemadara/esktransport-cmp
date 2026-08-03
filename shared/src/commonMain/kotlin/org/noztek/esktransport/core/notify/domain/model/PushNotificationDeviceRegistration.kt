package org.noztek.esktransport.core.notify.domain.model

data class PushNotificationDeviceRegistration(
    val platform: String,
    val token: String,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val appVersion: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
)
