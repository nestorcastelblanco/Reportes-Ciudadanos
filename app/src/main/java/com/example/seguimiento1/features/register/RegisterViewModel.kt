package com.example.seguimiento1.features.register

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.R
import com.example.seguimiento1.core.utils.FieldValidators
import com.example.seguimiento1.core.utils.ResourceProvider
import com.example.seguimiento1.core.utils.ValidatedField
import com.example.seguimiento1.domain.model.RegisterData
import com.example.seguimiento1.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _nombre = MutableStateFlow(ValidatedField())
    val nombre: StateFlow<ValidatedField> = _nombre.asStateFlow()

    private val _email = MutableStateFlow(ValidatedField())
    val email: StateFlow<ValidatedField> = _email.asStateFlow()

    private val _telefono = MutableStateFlow(ValidatedField())
    val telefono: StateFlow<ValidatedField> = _telefono.asStateFlow()

    private val _ciudad = MutableStateFlow(ValidatedField())
    val ciudad: StateFlow<ValidatedField> = _ciudad.asStateFlow()

    private val _password = MutableStateFlow(ValidatedField())
    val password: StateFlow<ValidatedField> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow(ValidatedField())
    val confirmPassword: StateFlow<ValidatedField> = _confirmPassword.asStateFlow()

    fun onNombreChange(value: String) {
        _nombre.value = _nombre.value.onChange(value) { v ->
            when {
                v.isEmpty() -> resourceProvider.getString(R.string.register_required_name)
                v.length < 3 -> resourceProvider.getString(R.string.register_name_min_length)
                else -> null
            }
        }
    }

    fun onEmailChange(value: String) {
        _email.value = _email.value.onChange(value) { v ->
            FieldValidators.email(v)?.let { resourceProvider.getString(it) }
        }
    }

    fun onTelefonoChange(value: String) {
        _telefono.value = _telefono.value.onChange(value) { v ->
            when {
                v.isEmpty() -> resourceProvider.getString(R.string.register_required_phone)
                v.length < 10 -> resourceProvider.getString(R.string.register_phone_min_length)
                !v.all { it.isDigit() } -> resourceProvider.getString(R.string.register_phone_digits_only)
                else -> null
            }
        }
    }

    fun onCiudadChange(value: String) {
        _ciudad.value = _ciudad.value.onChange(value) { v ->
            when {
                v.isEmpty() -> resourceProvider.getString(R.string.register_required_city)
                v.length < 3 -> resourceProvider.getString(R.string.register_invalid_city)
                else -> null
            }
        }
    }

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
        get() = listOf(_nombre, _email, _telefono, _ciudad, _password, _confirmPassword)
            .all { it.value.isTouched && it.value.isValid }

    suspend fun register(): Boolean {
        val data = RegisterData(
            nombre = _nombre.value.value,
            email = _email.value.value,
            telefono = _telefono.value.value,
            ciudad = _ciudad.value.value,
            password = _password.value.value
        )

        return authRepository.register(data)
    }
}