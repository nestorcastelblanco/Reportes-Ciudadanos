package com.example.seguimiento1.domain.model

data class CreateReportData(
    val title: String,
    val description: String,
    val address: String,
    val category: ReportCategory,
    val reporterEmail: String
)

