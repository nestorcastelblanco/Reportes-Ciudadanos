package com.example.seguimiento1.domain.model

data class Comment(
    val id: String,
    val reportId: String,
    val authorEmail: String,
    val authorName: String,
    val text: String,
    val createdAtMillis: Long
)
