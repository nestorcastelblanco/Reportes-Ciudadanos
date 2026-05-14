package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.domain.model.AppNotification
import com.uniquindio.reportes.domain.model.NotificationType
import com.uniquindio.reportes.domain.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio de notificaciones respaldado por Firebase Cloud Messaging.
 *
 * FCM entrega los mensajes al [com.uniquindio.reportes.features.notifications.ReportesFcmService],
 * que llama a [onMessageReceived] para acumularlos en el StateFlow expuesto a la UI.
 *
 * Es un Singleton compartido entre el servicio FCM y los ViewModels.
 */
@Singleton
class FcmNotificationRepository @Inject constructor() : NotificationRepository {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())

    override val notificationsFlow: Flow<List<AppNotification>> = _notifications.asStateFlow()

    fun onMessageReceived(
        title: String,
        message: String,
        typeRaw: String?
    ) {
        val type = parseType(typeRaw)
        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            message = message,
            createdAtMillis = System.currentTimeMillis()
        )
        _notifications.value = listOf(notification) + _notifications.value
    }

    private fun parseType(raw: String?): NotificationType {
        return runCatching {
            NotificationType.valueOf(raw.orEmpty().uppercase())
        }.getOrDefault(NotificationType.NEARBY_REPORT)
    }
}
