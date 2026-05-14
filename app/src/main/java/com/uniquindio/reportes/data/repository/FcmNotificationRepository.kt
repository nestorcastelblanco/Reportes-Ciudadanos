package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.data.notifications.FirestoreNotificationStore
import com.uniquindio.reportes.domain.model.AppNotification
import com.uniquindio.reportes.domain.model.NotificationType
import com.uniquindio.reportes.domain.repository.NotificationRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Repositorio de notificaciones.
 *
 * Combina dos fuentes:
 * 1. Colección `notifications` en Firestore, filtrada por el email de la sesión.
 *    Esta es la fuente principal: cualquier cliente (moderador, otro usuario)
 *    escribe ahí y el destinatario la ve en tiempo real.
 * 2. Mensajes FCM recibidos por [com.uniquindio.reportes.features.notifications.ReportesFcmService]
 *    cuando exista un backend que envíe push reales (Cloud Functions, plan Blaze).
 *
 * Mientras se opera en Spark sin Cloud Functions, (1) es el camino real.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class FcmNotificationRepository @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val firestoreStore: FirestoreNotificationStore
) : NotificationRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Notificaciones recibidas vía FCM directo (in-memory, no persistidas). */
    private val _fcmInbox = MutableStateFlow<List<AppNotification>>(emptyList())

    override val notificationsFlow: Flow<List<AppNotification>> =
        sessionRepository.sessionFlow
            .map { it.email.orEmpty() }
            .distinctUntilChanged()
            .flatMapLatest { email ->
                if (email.isBlank()) flowOf(emptyList())
                else firestoreStore.observeForUser(email)
            }
            .combine(_fcmInbox) { remote, local ->
                (local + remote)
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAtMillis }
            }

    /** Llamado por [com.uniquindio.reportes.features.notifications.ReportesFcmService]. */
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
        _fcmInbox.value = listOf(notification) + _fcmInbox.value
    }

    private fun parseType(raw: String?): NotificationType {
        return runCatching {
            NotificationType.valueOf(raw.orEmpty().uppercase())
        }.getOrDefault(NotificationType.NEARBY_REPORT)
    }
}
