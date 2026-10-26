package org.noztek.esktransport.feature.driver.notification.domain.model

data class DriverNotification(
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val status: String,
    val createdAt: String?,
    val sentAt: String?,
    val readAt: String?,
)

data class DriverNotificationsPage(
    val notifications: List<DriverNotification>,
    val unreadCount: Int,
)
