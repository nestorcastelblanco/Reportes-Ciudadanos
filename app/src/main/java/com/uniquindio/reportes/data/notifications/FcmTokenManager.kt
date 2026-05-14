package com.uniquindio.reportes.data.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/**
 * Guarda el FCM token del dispositivo en Firestore bajo `users/{email}.fcmToken`.
 *
 * Sin Cloud Functions el token no se usa para enviar push reales, pero queda
 * registrado para una migración futura a Blaze + Cloud Functions.
 */
@Singleton
class FcmTokenManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {

    suspend fun fetchAndPersist(email: String) {
        if (email.isBlank()) return
        runCatching {
            val token = messaging.token.await()
            persist(email, token)
        }
    }

    suspend fun persist(email: String, token: String) {
        if (email.isBlank() || token.isBlank()) return
        runCatching {
            firestore.collection(USERS).document(email)
                .set(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
        }
    }

    private companion object {
        const val USERS = "users"
    }
}
