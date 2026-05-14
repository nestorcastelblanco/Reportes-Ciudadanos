package com.uniquindio.reportes.data.notifications

import com.uniquindio.reportes.domain.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Inicia el ecosistema de notificaciones cuando hay sesión:
 * - Persiste el FCM token actual del dispositivo en `users/{email}`.
 * - Arranca el observador de reportes cercanos.
 */
@Singleton
class NotificationBootstrap @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val tokenManager: FcmTokenManager,
    private val nearbyObserver: NearbyReportObserver,
    private val foregroundNotifier: ForegroundSystemNotifier
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        nearbyObserver.start()
        foregroundNotifier.start()
        scope.launch {
            sessionRepository.sessionFlow
                .map { it.email.orEmpty() }
                .distinctUntilChanged()
                .collect { email ->
                    if (email.isNotBlank()) {
                        tokenManager.fetchAndPersist(email)
                    }
                }
        }
    }
}
