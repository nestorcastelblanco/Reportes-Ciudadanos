package com.example.seguimiento1.domain.repository

import com.example.seguimiento1.domain.model.SessionInfo
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val sessionFlow: Flow<SessionInfo>

    suspend fun saveSession(email: String)
    suspend fun clearSession()
}

