package org.noztek.esktransport.feature.driver.notification.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverNotificationsResponseDto(
    val data: List<DriverNotificationDto> = emptyList(),
    @SerialName("unread_count")
    val unreadCount: Int = 0,
)

@Serializable
data class DriverNotificationDto(
    val id: Long,
    val type: String = "notification",
    val title: String = "Notification",
    val body: String = "",
    val status: String = "sent",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("sent_at")
    val sentAt: String? = null,
    @SerialName("read_at")
    val readAt: String? = null,
)
