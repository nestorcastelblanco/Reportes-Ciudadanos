package com.uniquindio.reportes.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.uniquindio.reportes.MainActivity
import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.AppNotification
import com.uniquindio.reportes.domain.repository.SessionRepository
import com.uniquindio.reportes.features.notifications.ReportesFcmService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Dispara notificaciones de sistema (status bar) cuando llegan documentos
 * nuevos a `notifications` para el usuario en sesión, mientras la app está
 * en foreground.
 *
 * Compensa la falta de Cloud Functions: el listener Firestore actúa como
 * push real solo si la app está viva. El snapshot inicial se descarta para
 * no re-notificar al abrir la app.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class ForegroundSystemNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val store: FirestoreNotificationStore
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        ReportesFcmService.ensureChannel(context)
        job = scope.launch {
            sessionRepository.sessionFlow
                .map { it.email.orEmpty() }
                .distinctUntilChanged()
                .flatMapLatest { email ->
                    if (email.isBlank()) flowOf(emptyList())
                    else store.observeForUser(email)
                }
                .collect(::onSnapshot)
        }
    }

    private val seenIds = mutableSetOf<String>()
    private var initialized = false

    private fun onSnapshot(notifications: List<AppNotification>) {
        if (!initialized) {
            notifications.forEach { seenIds.add(it.id) }
            initialized = true
            return
        }
        val newOnes = notifications.filter { it.id !in seenIds }
        newOnes.forEach { seenIds.add(it.id) }
        newOnes.forEach(::post)
    }

    private fun post(notification: AppNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ReportesFcmService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(context)
            .notify(notification.id.hashCode(), builder.build())
    }
}
