package com.example.seguimiento1.domain.model

data class CreateReportData(
    val title: String,
    val description: String,
    val address: String,
    val category: ReportCategory,
    val reporterEmail: String,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

