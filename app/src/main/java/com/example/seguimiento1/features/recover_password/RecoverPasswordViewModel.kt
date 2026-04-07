package com.example.seguimiento1.features.recover_password

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.di.RepositoryModule
import com.example.seguimiento1.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecoverPasswordViewModel(
    private val authRepository: AuthRepository = RepositoryModule.authRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    fun onEmailChange(value: String) {
        _email.value = value
    }

    suspend fun recover(): Boolean {
        return authRepository.sendRecovery(email.value)
    }
}