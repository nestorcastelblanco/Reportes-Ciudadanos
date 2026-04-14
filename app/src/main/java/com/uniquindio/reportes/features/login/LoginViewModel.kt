package com.uniquindio.reportes.features.login

import androidx.lifecycle.ViewModel
import com.uniquindio.reportes.core.utils.FieldValidators
import com.uniquindio.reportes.core.utils.ResourceProvider
import com.uniquindio.reportes.core.utils.ValidatedField
import com.uniquindio.reportes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _email = MutableStateFlow(ValidatedField())
    val email: StateFlow<ValidatedField> = _email.asStateFlow()

    private val _password = MutableStateFlow(ValidatedField())
    val password: StateFlow<ValidatedField> = _password.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = _email.value.onChange(value) { v ->
            FieldValidators.email(v)?.let { resourceProvider.getString(it) }
        }
    }

    fun onPasswordChange(value: String) {
        _password.value = _password.value.onChange(value) { v ->
            FieldValidators.password(v)?.let { resourceProvider.getString(it) }
        }
    }

    val isFormValid: Boolean
        get() = _email.value.isTouched && _email.value.isValid &&
                _password.value.isTouched && _password.value.isValid

    suspend fun login(): Boolean {
        return authRepository.login(_email.value.value, _password.value.value)
    }
}