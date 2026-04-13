package com.example.seguimiento1.data.repository

import com.example.seguimiento1.domain.model.AppNotification
import com.example.seguimiento1.domain.model.NotificationType
import com.example.seguimiento1.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryNotificationRepository : NotificationRepository {

    private val _notifications = MutableStateFlow(
        listOf(
            AppNotification(
                id = "n1",
                type = NotificationType.NEARBY_REPORT,
                title = "Nuevo reporte cercano",
                message = "Se reportó un incidente de seguridad a 300m de tu ubicación.",
                createdAtMillis = System.currentTimeMillis() - 900_000 // 15 min
            ),
            AppNotification(
                id = "n2",
                type = NotificationType.NEW_COMMENT,
                title = "Nuevo comentario",
                message = "Maria Lopez comentó en tu reporte 'Robo de bicicleta...'",
                createdAtMillis = System.currentTimeMillis() - 3_600_000 // 1h
            ),
            AppNotification(
                id = "n3",
                type = NotificationType.REPORT_VERIFIED,
                title = "Reporte verificado",
                message = "Tu reporte 'Poste de alumbrado caído' fue verificado por un moderador.",
                createdAtMillis = System.currentTimeMillis() - 10_800_000 // 3h
            ),
            AppNotification(
                id = "n4",
                type = NotificationType.NEARBY_REPORT,
                title = "Nuevo reporte cercano",
                message = "Se reportó un incidente de seguridad a 300m de tu ubicación.",
                createdAtMillis = System.currentTimeMillis() - 86_400_000 // 1d
            ),
            AppNotification(
                id = "n5",
                type = NotificationType.NEARBY_REPORT,
                title = "Nuevo reporte cercano",
                message = "Se reportó un incidente de seguridad a 300m de tu ubicación.",
                createdAtMillis = System.currentTimeMillis() - 259_200_000 // 3d
            )
        )
    )

    override val notificationsFlow: Flow<List<AppNotification>> = _notifications
}
