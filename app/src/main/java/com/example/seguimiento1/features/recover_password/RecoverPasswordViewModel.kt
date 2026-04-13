package com.example.seguimiento1.features.recover_password

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
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