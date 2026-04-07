package com.example.seguimiento1.features.login

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.di.RepositoryModule
import com.example.seguimiento1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(
    private val authRepository: AuthRepository = RepositoryModule.authRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    suspend fun login(): Boolean {
        return authRepository.login(email.value, password.value)
    }
}