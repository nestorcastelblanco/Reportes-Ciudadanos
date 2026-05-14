package com.uniquindio.reportes.features.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uniquindio.reportes.MainActivity
import com.uniquindio.reportes.R
import com.uniquindio.reportes.data.repository.FcmNotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Servicio que recibe los mensajes de Firebase Cloud Messaging.
 *
 * - Inserta el mensaje en el [FcmNotificationRepository] para que la UI lo muestre.
 * - Muestra una notificación de sistema cuando la app está en background.
 */
@AndroidEntryPoint
class ReportesFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: FcmNotificationRepository

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notif = message.notification
        val data = message.data

        val title = notif?.title ?: data["title"].orEmpty().ifBlank { getString(R.string.app_name) }
        val body = notif?.body ?: data["message"].orEmpty()
        val type = data["type"]

        notificationRepository.onMessageReceived(title, body, type)
        showSystemNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: enviar token al backend cuando exista el endpoint.
    }

    private fun showSystemNotification(title: String, body: String) {
        ensureChannel(this)

        val intent = MainActivity::class.java.let { cls ->
            android.content.Intent(this, cls).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingFlags)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
    }

    companion object {
        const val CHANNEL_ID = "reportes_ciudadanos_default"

        fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de Reportes Ciudadanos"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
