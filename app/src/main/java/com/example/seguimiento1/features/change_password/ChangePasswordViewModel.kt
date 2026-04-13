package com.example.seguimiento1.features.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.core.utils.FieldValidators
import com.example.seguimiento1.core.utils.ResourceProvider
import com.example.seguimiento1.core.utils.ValidatedField
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
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _oldPassword = MutableStateFlow(ValidatedField())
    val oldPassword: StateFlow<ValidatedField> = _oldPassword.asStateFlow()

    private val _newPassword = MutableStateFlow(ValidatedField())
    val newPassword: StateFlow<ValidatedField> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow(ValidatedField())
    val confirmPassword: StateFlow<ValidatedField> = _confirmPassword.asStateFlow()

    fun onOldPasswordChange(value: String) {
        _oldPassword.value = _oldPassword.value.onChange(value) { _ -> null }
    }

    fun onNewPasswordChange(value: String) {
        _newPassword.value = _newPassword.value.onChange(value) { v ->
            FieldValidators.password(v)?.let { resourceProvider.getString(it) }
        }
        if (_confirmPassword.value.isTouched) {
            _confirmPassword.value = _confirmPassword.value.onChange(_confirmPassword.value.value) { v ->
                FieldValidators.confirmPassword(value, v)?.let { resourceProvider.getString(it) }
            }
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = _confirmPassword.value.onChange(value) { v ->
            FieldValidators.confirmPassword(_newPassword.value.value, v)?.let { resourceProvider.getString(it) }
        }
    }

    val isFormValid: Boolean
        get() = _oldPassword.value.value.isNotBlank() &&
                _newPassword.value.isTouched && _newPassword.value.isValid &&
                _confirmPassword.value.isTouched && _confirmPassword.value.isValid

    fun changePassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            val result = authRepository.changePassword(email, _oldPassword.value.value, _newPassword.value.value)
            if (result) {
                onSuccess()
            } else {
                onError("wrong_old")
            }
        }
    }
}
