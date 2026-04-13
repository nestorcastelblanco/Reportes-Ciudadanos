package com.example.seguimiento1.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.User
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _email = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            _email.value = email
            _user.value = authRepository.getUserByEmail(email)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _user.value = authRepository.getUserByEmail(_email.value)
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount(_email.value)
            sessionRepository.clearSession()
            onSuccess()
        }
    }
}
