package com.uniquindio.reportes.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.uniquindio.reportes.domain.repository.ImageStorageRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Sube imágenes a Firebase Storage bajo la ruta `reports/{email}/{uuid}.jpg`
 * y devuelve la URL de descarga.
 */
@Singleton
class FirebaseImageStorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) : ImageStorageRepository {

    override suspend fun uploadReportImage(localUri: String, reporterEmail: String): String =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(localUri)
            val safeEmail = reporterEmail.ifBlank { "anonymous" }
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference
                .child(PATH_REPORTS)
                .child(safeEmail)
                .child(filename)
            Log.d(TAG, "uploading $localUri -> ${ref.path}")
            try {
                ref.putFile(uri).await()
                val url = ref.downloadUrl.await().toString()
                Log.d(TAG, "upload OK -> $url")
                url
            } catch (e: Exception) {
                Log.e(TAG, "upload FAILED for $localUri: ${e::class.java.simpleName}: ${e.message}", e)
                throw e
            }
        }

    override suspend fun uploadReportImages(
        localUris: List<String>,
        reporterEmail: String
    ): List<String> = coroutineScope {
        localUris
            .map { local ->
                async {
                    if (local.startsWith("http://") || local.startsWith("https://")) {
                        local
                    } else {
                        uploadReportImage(local, reporterEmail)
                    }
                }
            }
            .awaitAll()
    }

    private companion object {
        const val PATH_REPORTS = "reports"
        const val TAG = "FirebaseStorageRepo"
    }
}
