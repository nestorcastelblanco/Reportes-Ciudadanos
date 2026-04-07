package com.example.seguimiento1.data.repository

import com.example.seguimiento1.data.datastore.SessionDataStore
import com.example.seguimiento1.domain.model.SessionInfo
import com.example.seguimiento1.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SessionRepositoryImpl(
    private val sessionDataStore: SessionDataStore
) : SessionRepository {

    override val sessionFlow: Flow<SessionInfo> = combine(
        sessionDataStore.isLoggedInFlow,
        sessionDataStore.emailFlow
    ) { isLoggedIn, email ->
        SessionInfo(isLoggedIn = isLoggedIn, email = email)
    }

    override suspend fun saveSession(email: String) {
        sessionDataStore.saveSession(email)
    }

    override suspend fun clearSession() {
        sessionDataStore.clearSession()
    }
}

