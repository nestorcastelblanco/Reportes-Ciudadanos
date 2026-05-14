package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.ReportCategory

/**
 * Clasifica un reporte ciudadano en una de las categorías disponibles
 * a partir del texto libre (título + descripción).
 */
interface CategoryClassifier {
    suspend fun classify(title: String, description: String): ReportCategory?
}
