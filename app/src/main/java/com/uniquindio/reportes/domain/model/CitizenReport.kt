package com.uniquindio.reportes.domain.model

data class CitizenReport(
    val id: String,
    val title: String,
    val description: String,
    val address: String,
    val category: ReportCategory,
    val status: ReportStatus,
    val imageUrl: String,
    val reporterEmail: String,
    val reporterName: String = "",
    val createdAtMillis: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val importance: Int = 0,
    val voterEmails: List<String> = emptyList(),
    val isResolved: Boolean = false
)

