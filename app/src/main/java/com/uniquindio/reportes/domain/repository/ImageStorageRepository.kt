package com.uniquindio.reportes.domain.repository

/**
 * Sube imágenes (locales: content:// o file://) a almacenamiento remoto
 * y devuelve URLs públicas/descargables.
 */
interface ImageStorageRepository {
    suspend fun uploadReportImage(localUri: String, reporterEmail: String): String
    suspend fun uploadReportImages(localUris: List<String>, reporterEmail: String): List<String>
}
