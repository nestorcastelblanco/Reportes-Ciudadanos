package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val notificationsFlow: Flow<List<AppNotification>>
}
