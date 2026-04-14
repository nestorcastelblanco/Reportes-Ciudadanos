package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.domain.model.Comment
import com.uniquindio.reportes.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryCommentRepository : CommentRepository {

    private val comments = MutableStateFlow(
        listOf(
            Comment(
                id = "c1",
                reportId = "seed-1",
                authorEmail = "maria@ciudad.com",
                authorName = "Maria Lopez",
                text = "Confirmo, yo tambien vi esta situacion.",
                createdAtMillis = System.currentTimeMillis() - 3_600_000
            ),
            Comment(
                id = "c2",
                reportId = "seed-1",
                authorEmail = "david@ciudad.com",
                authorName = "David Botero",
                text = "Complicado el paso.",
                createdAtMillis = System.currentTimeMillis() - 5_400_000
            ),
            Comment(
                id = "c3",
                reportId = "seed-1",
                authorEmail = "julian@ciudad.com",
                authorName = "Julián Ladino",
                text = "¿Si es verdad?",
                createdAtMillis = System.currentTimeMillis() - 7_200_000
            )
        )
    )

    override fun commentsForReport(reportId: String): Flow<List<Comment>> {
        return comments.map { list -> list.filter { it.reportId == reportId } }
    }

    override suspend fun addComment(comment: Comment) {
        comments.value = comments.value + comment
    }
}
