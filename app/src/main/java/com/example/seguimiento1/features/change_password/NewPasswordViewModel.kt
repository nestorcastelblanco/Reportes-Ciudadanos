package com.example.seguimiento1.features.change_password

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.core.utils.FieldValidators
import com.example.seguimiento1.core.utils.ResourceProvider
import com.example.seguimiento1.core.utils.ValidatedField
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _password = MutableStateFlow(ValidatedField())
    val password: StateFlow<ValidatedField> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow(ValidatedField())
    val confirmPassword: StateFlow<ValidatedField> = _confirmPassword.asStateFlow()

    fun onPasswordChange(value: String) {
        _password.value = _password.value.onChange(value) { v ->
            FieldValidators.password(v)?.let { resourceProvider.getString(it) }
        }
        // Re-validate confirm if already touched
        if (_confirmPassword.value.isTouched) {
            _confirmPassword.value = _confirmPassword.value.onChange(_confirmPassword.value.value) { v ->
                FieldValidators.confirmPassword(value, v)?.let { resourceProvider.getString(it) }
            }
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _confirmPassword.value = _confirmPassword.value.onChange(value) { v ->
            FieldValidators.confirmPassword(_password.value.value, v)?.let { resourceProvider.getString(it) }
        }
    }

    val isFormValid: Boolean
        get() = _password.value.isTouched && _password.value.isValid &&
                _confirmPassword.value.isTouched && _confirmPassword.value.isValid
}

