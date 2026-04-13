package com.example.seguimiento1.domain.repository

import com.example.seguimiento1.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun commentsForReport(reportId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
}
