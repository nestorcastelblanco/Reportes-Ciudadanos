package com.uniquindio.reportes.features.recover_password

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
class RecoverPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _email = MutableStateFlow(ValidatedField())
    val email: StateFlow<ValidatedField> = _email.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = _email.value.onChange(value) { v ->
            FieldValidators.email(v)?.let { resourceProvider.getString(it) }
        }
    }

    suspend fun recover(): Boolean {
        return authRepository.sendRecovery(_email.value.value)
    }
}