package com.uniquindio.reportes.domain.model

enum class NotificationType {
    NEARBY_REPORT,
    NEW_COMMENT,
    REPORT_VERIFIED,
    REPORT_REJECTED
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val createdAtMillis: Long,
    val reportId: String? = null,
    val read: Boolean = false
)
