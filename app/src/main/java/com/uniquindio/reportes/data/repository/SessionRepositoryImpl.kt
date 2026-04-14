package com.uniquindio.reportes.data.repository

import com.uniquindio.reportes.data.datastore.SessionDataStore
import com.uniquindio.reportes.domain.model.SessionInfo
import com.uniquindio.reportes.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
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

