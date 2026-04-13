package com.example.seguimiento1.domain.model

enum class NotificationType {
    NEARBY_REPORT,
    NEW_COMMENT,
    REPORT_VERIFIED
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val createdAtMillis: Long
)
