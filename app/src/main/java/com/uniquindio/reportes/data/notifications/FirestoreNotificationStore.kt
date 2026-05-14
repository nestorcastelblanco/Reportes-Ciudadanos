package com.uniquindio.reportes.data.notifications

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uniquindio.reportes.domain.model.AppNotification
import com.uniquindio.reportes.domain.model.NotificationType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Lectura y escritura de la colección `notifications` en Firestore.
 *
 * Como reemplazo a Cloud Functions: los clientes que ejecutan una acción
 * (aprobar/rechazar reporte, comentar, etc.) escriben directamente la
 * notificación destinada al usuario receptor. El destinatario la verá la
 * próxima vez que el listener emita.
 */
@Singleton
class FirestoreNotificationStore @Inject constructor(
    firestore: FirebaseFirestore
) {

    private val collection = firestore.collection(COLLECTION_NOTIFICATIONS)

    fun observeForUser(email: String): Flow<List<AppNotification>> = callbackFlow {
        if (email.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = collection
            .whereEqualTo("recipientEmail", email)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toNotification() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    suspend fun publish(
        recipientEmail: String,
        type: NotificationType,
        title: String,
        message: String,
        reportId: String? = null
    ) {
        if (recipientEmail.isBlank()) return
        val id = UUID.randomUUID().toString()
        runCatching {
            collection.document(id).set(
                mapOf(
                    "id" to id,
                    "recipientEmail" to recipientEmail,
                    "type" to type.name,
                    "title" to title,
                    "message" to message,
                    "reportId" to reportId,
                    "createdAtMillis" to System.currentTimeMillis(),
                    "read" to false
                )
            ).await()
        }
    }

    suspend fun markRead(id: String) {
        runCatching {
            collection.document(id).update("read", true).await()
        }
    }

    private fun DocumentSnapshot.toNotification(): AppNotification? {
        if (!exists()) return null
        return runCatching {
            val typeName = getString("type").orEmpty()
            val type = runCatching { NotificationType.valueOf(typeName) }
                .getOrDefault(NotificationType.NEARBY_REPORT)
            AppNotification(
                id = getString("id") ?: id,
                type = type,
                title = getString("title").orEmpty(),
                message = getString("message").orEmpty(),
                createdAtMillis = getLong("createdAtMillis") ?: 0L,
                reportId = getString("reportId"),
                read = getBoolean("read") ?: false
            )
        }.getOrNull()
    }

    private companion object {
        const val COLLECTION_NOTIFICATIONS = "notifications"
    }
}
