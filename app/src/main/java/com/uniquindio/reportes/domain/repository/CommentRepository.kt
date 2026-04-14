package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    val commentsFlow: Flow<List<Comment>>
    fun commentsForReport(reportId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
}
