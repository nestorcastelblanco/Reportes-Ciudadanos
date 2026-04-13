package com.example.seguimiento1.features.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ChangePasswordViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _oldPassword = MutableStateFlow("")
    val oldPassword: StateFlow<String> = _oldPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    fun onOldPasswordChange(v: String) { _oldPassword.value = v }
    fun onNewPasswordChange(v: String) { _newPassword.value = v }
    fun onConfirmPasswordChange(v: String) { _confirmPassword.value = v }

    fun changePassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            val result = authRepository.changePassword(email, _oldPassword.value, _newPassword.value)
            if (result) {
                onSuccess()
            } else {
                onError("wrong_old")
            }
        }
    }
}
