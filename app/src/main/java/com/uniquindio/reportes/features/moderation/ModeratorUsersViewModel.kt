package com.uniquindio.reportes.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.model.UserRole
import com.uniquindio.reportes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ModeratorUsersViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _users.value = authRepository.listUsers()
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun filtered(): List<User> {
        val q = _query.value.trim()
        if (q.isBlank()) return _users.value
        return _users.value.filter {
            it.nombre.contains(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true)
        }
    }
}

@HiltViewModel
class ModeratorUserDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun load(email: String) {
        viewModelScope.launch {
            _user.value = authRepository.getUserByEmail(email)
        }
    }

    fun changeRole(email: String, role: UserRole) {
        viewModelScope.launch {
            authRepository.setUserRole(email, role)
            load(email)
        }
    }

    fun setActive(email: String, active: Boolean) {
        viewModelScope.launch {
            authRepository.setUserActive(email, active)
            load(email)
        }
    }
}
