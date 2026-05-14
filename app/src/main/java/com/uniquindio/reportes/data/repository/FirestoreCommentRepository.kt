package com.uniquindio.reportes.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uniquindio.reportes.domain.model.Comment
import com.uniquindio.reportes.domain.repository.CommentRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de comentarios respaldado por Firestore.
 * Colección `comments`, un documento por comentario, indexado por `reportId`.
 */
@Singleton
class FirestoreCommentRepository @Inject constructor(
    firestore: FirebaseFirestore
) : CommentRepository {

    private val commentsCollection = firestore.collection(COLLECTION_COMMENTS)

    override val commentsFlow: Flow<List<Comment>> = callbackFlow {
        val registration = commentsCollection
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toComment() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override fun commentsForReport(reportId: String): Flow<List<Comment>> = callbackFlow {
        val registration = commentsCollection
            .whereEqualTo("reportId", reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toComment() }
                    ?.sortedByDescending { it.createdAtMillis }
                    .orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addComment(comment: Comment) {
        val id = comment.id.ifBlank { UUID.randomUUID().toString() }
        val finalComment = if (comment.id.isBlank()) comment.copy(id = id) else comment
        commentsCollection.document(id).set(finalComment.toMap()).await()
    }

    private fun Comment.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "reportId" to reportId,
        "authorEmail" to authorEmail,
        "authorName" to authorName,
        "text" to text,
        "createdAtMillis" to createdAtMillis
    )

    private fun DocumentSnapshot.toComment(): Comment? {
        if (!exists()) return null
        return runCatching {
            Comment(
                id = getString("id") ?: id,
                reportId = getString("reportId").orEmpty(),
                authorEmail = getString("authorEmail").orEmpty(),
                authorName = getString("authorName").orEmpty(),
                text = getString("text").orEmpty(),
                createdAtMillis = getLong("createdAtMillis") ?: 0L
            )
        }.getOrNull()
    }

    private companion object {
        const val COLLECTION_COMMENTS = "comments"
    }
}
