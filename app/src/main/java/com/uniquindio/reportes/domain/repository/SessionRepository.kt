package com.uniquindio.reportes.domain.repository

import com.uniquindio.reportes.domain.model.SessionInfo
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val sessionFlow: Flow<SessionInfo>

    suspend fun saveSession(email: String)
    suspend fun clearSession()
}

