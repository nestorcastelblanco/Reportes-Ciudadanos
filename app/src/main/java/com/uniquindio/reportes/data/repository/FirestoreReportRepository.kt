package com.uniquindio.reportes.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uniquindio.reportes.data.notifications.FirestoreNotificationStore
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.NotificationType
import com.uniquindio.reportes.domain.model.CreateReportData
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.repository.ReportRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de reportes respaldado por Firestore.
 *
 * Colección `reports`, documento por reporte.
 * El caché offline se habilita globalmente en FirebaseModule, así que
 * lecturas y escrituras funcionan sin conexión y se sincronizan al volver.
 */
@Singleton
class FirestoreReportRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val notificationStore: FirestoreNotificationStore
) : ReportRepository {

    private val reportsCollection = firestore.collection(COLLECTION_REPORTS)

    override val reportsFlow: Flow<List<CitizenReport>> = callbackFlow {
        val registration = reportsCollection
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toReport() }.orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun reportById(reportId: String): Flow<CitizenReport?> = callbackFlow {
        val registration = reportsCollection.document(reportId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toReport())
            }
        awaitClose { registration.remove() }
    }

    override fun reportsByEmail(email: String): Flow<List<CitizenReport>> = callbackFlow {
        val registration = reportsCollection
            .whereEqualTo("reporterEmail", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toReport() }
                    ?.sortedByDescending { it.createdAtMillis }
                    .orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createReport(data: CreateReportData) {
        val id = UUID.randomUUID().toString()
        val report = CitizenReport(
            id = id,
            title = data.title,
            description = data.description,
            address = data.address,
            category = data.category,
            status = ReportStatus.PENDING,
            imageUrls = data.imageUrls,
            reporterEmail = data.reporterEmail,
            reporterName = data.reporterName,
            createdAtMillis = System.currentTimeMillis(),
            latitude = data.latitude,
            longitude = data.longitude
        )
        reportsCollection.document(id).set(report.toMap()).await()
    }

    override suspend fun updateReport(report: CitizenReport) {
        reportsCollection.document(report.id).set(report.toMap()).await()
    }

    override suspend fun deleteReport(reportId: String) {
        reportsCollection.document(reportId).delete().await()
    }

    override suspend fun toggleImportance(reportId: String, voterEmail: String) {
        val docRef = reportsCollection.document(reportId)
        docRef.firestore.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            val report = snapshot.toReport() ?: return@runTransaction null
            val alreadyVoted = voterEmail in report.voterEmails
            val updated = if (alreadyVoted) {
                report.copy(
                    importance = report.importance - 1,
                    voterEmails = report.voterEmails - voterEmail
                )
            } else {
                report.copy(
                    importance = report.importance + 1,
                    voterEmails = report.voterEmails + voterEmail
                )
            }
            tx.set(docRef, updated.toMap())
        }.await()
    }

    override suspend fun verifyReport(reportId: String) {
        val report = fetchReport(reportId)
        reportsCollection.document(reportId)
            .update("status", ReportStatus.VERIFIED.name)
            .await()
        report?.let {
            notificationStore.publish(
                recipientEmail = it.reporterEmail,
                type = NotificationType.REPORT_VERIFIED,
                title = "Reporte aprobado",
                message = "Tu reporte \"${it.title}\" fue verificado por un moderador.",
                reportId = it.id
            )
        }
    }

    override suspend fun rejectReport(reportId: String) {
        val report = fetchReport(reportId)
        reportsCollection.document(reportId)
            .update("status", ReportStatus.REJECTED.name)
            .await()
        report?.let {
            notificationStore.publish(
                recipientEmail = it.reporterEmail,
                type = NotificationType.REPORT_REJECTED,
                title = "Reporte rechazado",
                message = "Tu reporte \"${it.title}\" fue rechazado por un moderador.",
                reportId = it.id
            )
        }
    }

    private suspend fun fetchReport(reportId: String): CitizenReport? {
        return runCatching {
            reportsCollection.document(reportId).get().await().toReport()
        }.getOrNull()
    }

    override suspend fun markResolved(reportId: String) {
        reportsCollection.document(reportId)
            .update(
                mapOf(
                    "status" to ReportStatus.RESOLVED.name,
                    "isResolved" to true
                )
            )
            .await()
    }

    private fun CitizenReport.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "address" to address,
        "category" to category.name,
        "status" to status.name,
        "imageUrls" to imageUrls,
        "reporterEmail" to reporterEmail,
        "reporterName" to reporterName,
        "createdAtMillis" to createdAtMillis,
        "latitude" to latitude,
        "longitude" to longitude,
        "importance" to importance,
        "voterEmails" to voterEmails,
        "isResolved" to isResolved
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toReport(): CitizenReport? {
        if (!exists()) return null
        return runCatching {
            CitizenReport(
                id = getString("id") ?: id,
                title = getString("title").orEmpty(),
                description = getString("description").orEmpty(),
                address = getString("address").orEmpty(),
                category = (getString("category")?.let {
                    runCatching { ReportCategory.valueOf(it) }.getOrNull()
                }) ?: ReportCategory.COMMUNITY,
                status = (getString("status")?.let {
                    runCatching { ReportStatus.valueOf(it) }.getOrNull()
                }) ?: ReportStatus.PENDING,
                imageUrls = (get("imageUrls") as? List<String>).orEmpty(),
                reporterEmail = getString("reporterEmail").orEmpty(),
                reporterName = getString("reporterName").orEmpty(),
                createdAtMillis = getLong("createdAtMillis") ?: 0L,
                latitude = getDouble("latitude"),
                longitude = getDouble("longitude"),
                importance = (getLong("importance") ?: 0L).toInt(),
                voterEmails = (get("voterEmails") as? List<String>).orEmpty(),
                isResolved = getBoolean("isResolved") ?: false
            )
        }.getOrNull()
    }

    private companion object {
        const val COLLECTION_REPORTS = "reports"
    }
}
