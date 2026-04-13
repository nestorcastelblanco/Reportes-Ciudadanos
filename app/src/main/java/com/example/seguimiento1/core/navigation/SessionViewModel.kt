package com.example.seguimiento1.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.UserRole
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class Authenticated(val email: String?, val role: UserRole = UserRole.USER) : SessionUiState
    data object Unauthenticated : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val sessionState: StateFlow<SessionUiState> = _sessionState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.sessionFlow.collect { session ->
                if (session.isLoggedIn && session.email != null) {
                    val userExists = authRepository.getUserByEmail(session.email) != null
                    if (userExists) {
                        val role = authRepository.getUserRole(session.email)
                        _sessionState.value = SessionUiState.Authenticated(email = session.email, role = role)
                    } else {
                        // Persisted session refers to a user that no longer exists (in-memory reset)
                        sessionRepository.clearSession()
                    }
                } else if (session.isLoggedIn) {
                    _sessionState.value = SessionUiState.Authenticated(email = session.email)
                } else {
                    _sessionState.value = SessionUiState.Unauthenticated
                }
            }
        }
    }

    fun onLoginSuccess(email: String) {
        viewModelScope.launch {
            sessionRepository.saveSession(email)
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionRepository.clearSession()
        }
    }
}

