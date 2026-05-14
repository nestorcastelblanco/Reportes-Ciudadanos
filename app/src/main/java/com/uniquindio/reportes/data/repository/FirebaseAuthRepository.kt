package com.uniquindio.reportes.data.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.uniquindio.reportes.domain.model.RegisterData
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.model.UserRole
import com.uniquindio.reportes.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de AuthRepository basada en Firebase Auth + Firestore.
 *
 * Estructura en Firestore:
 * - Colección `users`, documento con ID = email del usuario
 *   campos: nombre, telefono, ciudad, role, joinDateMillis, points, profilePhotoUrl
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection get() = firestore.collection(COLLECTION_USERS)

    override suspend fun login(email: String, password: String): Boolean {
        return runCatching {
            auth.signInWithEmailAndPassword(email, password).await()
        }.onFailure { e ->
            Log.e(TAG, "login failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }.isSuccess
    }

    override suspend fun register(data: RegisterData): Boolean {
        Log.d(TAG, "register attempt for '${data.email}'")
        return runCatching {
            val result = auth.createUserWithEmailAndPassword(data.email, data.password).await()
            Log.d(TAG, "createUserWithEmailAndPassword OK, uid=${result.user?.uid}")
            val user = User(
                email = data.email,
                nombre = data.nombre,
                telefono = data.telefono,
                ciudad = data.ciudad,
                role = UserRole.USER
            )
            usersCollection.document(data.email).set(user.toMap()).await()
            Log.d(TAG, "user profile written to Firestore: users/${data.email}")
        }.onFailure { e ->
            Log.e(
                TAG,
                "register failed for '${data.email}': ${e::class.java.simpleName}: ${e.message}",
                e
            )
        }.isSuccess
    }

    override suspend fun sendRecovery(email: String): Boolean {
        return runCatching {
            auth.sendPasswordResetEmail(email).await()
        }.onFailure { e ->
            Log.e(TAG, "sendRecovery failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }.isSuccess
    }

    override suspend fun changePassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): Boolean {
        val current = auth.currentUser ?: return false
        if (current.email != email) return false
        return runCatching {
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            current.reauthenticate(credential).await()
            current.updatePassword(newPassword).await()
        }.onFailure { e ->
            Log.e(TAG, "changePassword failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }.isSuccess
    }

    override suspend fun getUserByEmail(email: String): User? {
        return runCatching {
            val snapshot = usersCollection.document(email).get().await()
            if (!snapshot.exists()) null else snapshot.toUser(email)
        }.onFailure { e ->
            Log.e(TAG, "getUserByEmail failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }.getOrNull()
    }

    override suspend fun updateUser(user: User) {
        runCatching {
            usersCollection.document(user.email).set(user.toMap()).await()
        }.onFailure { e ->
            Log.e(TAG, "updateUser failed for '${user.email}': ${e::class.java.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun deleteAccount(email: String): Boolean {
        val current = auth.currentUser ?: return false
        if (current.email != email) return false
        return runCatching {
            usersCollection.document(email).delete().await()
            current.delete().await()
        }.onFailure { e ->
            Log.e(TAG, "deleteAccount failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }.isSuccess
    }

    override suspend fun getUserRole(email: String): UserRole {
        return getUserByEmail(email)?.role ?: UserRole.USER
    }

    override suspend fun listUsers(): List<User> {
        return runCatching {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toUser(doc.id)
            }.sortedBy { it.nombre.ifBlank { it.email } }
        }.onFailure { e ->
            Log.e(TAG, "listUsers failed: ${e::class.java.simpleName}: ${e.message}", e)
        }.getOrDefault(emptyList())
    }

    override suspend fun setUserRole(email: String, role: UserRole) {
        runCatching {
            usersCollection.document(email).update("role", role.name).await()
        }.onFailure { e ->
            Log.e(TAG, "setUserRole failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun setUserActive(email: String, active: Boolean) {
        runCatching {
            usersCollection.document(email).update("active", active).await()
        }.onFailure { e ->
            Log.e(TAG, "setUserActive failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }
    }

    override suspend fun addPoints(email: String, points: Int) {
        runCatching {
            usersCollection.document(email)
                .update("points", FieldValue.increment(points.toLong()))
                .await()
        }.onFailure { e ->
            Log.e(TAG, "addPoints failed for '$email': ${e::class.java.simpleName}: ${e.message}", e)
        }
    }

    private fun User.toMap(): Map<String, Any?> = mapOf(
        "email" to email,
        "nombre" to nombre,
        "telefono" to telefono,
        "ciudad" to ciudad,
        "role" to role.name,
        "joinDateMillis" to joinDateMillis,
        "points" to points,
        "profilePhotoUrl" to profilePhotoUrl,
        "active" to active
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(email: String): User {
        val roleName = getString("role") ?: UserRole.USER.name
        return User(
            email = email,
            nombre = getString("nombre").orEmpty(),
            telefono = getString("telefono").orEmpty(),
            ciudad = getString("ciudad").orEmpty(),
            role = runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.USER),
            joinDateMillis = getLong("joinDateMillis") ?: System.currentTimeMillis(),
            points = (getLong("points") ?: 0L).toInt(),
            profilePhotoUrl = getString("profilePhotoUrl"),
            active = getBoolean("active") ?: true
        )
    }

    private companion object {
        const val COLLECTION_USERS = "users"
        const val TAG = "FirebaseAuthRepo"
    }
}
