package org.noztek.esktransport.feature.common.notification.domain.model

data class Notification(
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val status: String,
    val createdAt: String?,
    val sentAt: String?,
    val readAt: String?,
)

data class NotificationsPage(
    val notifications: List<Notification>,
    val unreadCount: Int,
)
