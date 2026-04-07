package com.example.seguimiento1.core.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.di.RepositoryModule
import com.example.seguimiento1.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class Authenticated(val email: String?) : SessionUiState
    data object Unauthenticated : SessionUiState
}

class SessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val sessionState: StateFlow<SessionUiState> = _sessionState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.sessionFlow.collect { session ->
                _sessionState.value = if (session.isLoggedIn) {
                    SessionUiState.Authenticated(email = session.email)
                } else {
                    SessionUiState.Unauthenticated
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

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SessionViewModel(
                        sessionRepository = RepositoryModule.provideSessionRepository(appContext)
                    ) as T
                }
            }
        }
    }
}

