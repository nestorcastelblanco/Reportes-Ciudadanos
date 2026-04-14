package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun commentsForReport(reportId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
}
