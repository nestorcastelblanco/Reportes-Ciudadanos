package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.CreateReportData
import com.uniquindio.reportes.domain.model.ReportCategory
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    val reportsFlow: Flow<List<CitizenReport>>
    fun reportById(reportId: String): Flow<CitizenReport?>
    fun reportsByEmail(email: String): Flow<List<CitizenReport>>
    suspend fun createReport(data: CreateReportData)
    suspend fun updateReport(report: CitizenReport)
    suspend fun deleteReport(reportId: String)
    suspend fun toggleImportance(reportId: String, voterEmail: String)
    suspend fun verifyReport(reportId: String)
    suspend fun rejectReport(reportId: String)
    suspend fun markResolved(reportId: String)
}

