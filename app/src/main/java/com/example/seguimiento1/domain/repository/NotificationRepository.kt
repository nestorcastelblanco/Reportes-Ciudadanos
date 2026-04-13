package com.example.seguimiento1.domain.repository

import com.example.seguimiento1.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val notificationsFlow: Flow<List<AppNotification>>
}
