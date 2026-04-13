package com.example.seguimiento1.features.reputation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.Badge
import com.example.seguimiento1.domain.model.User
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
class ReputationViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val badges = MutableStateFlow(
        listOf(
            Badge("b1", "Primera publicación", "Publica tu primer reporte", true),
            Badge("b2", "10 verificados", "10 reportes verificados", true),
            Badge("b3", "Destacado del mes", "Reporte más votado del mes", true),
            Badge("b4", "50 comentarios", "Deja 50 comentarios", true),
            Badge("b5", "100 votos recibidos", "Recibe 100 votos", true),
            Badge("b6", "Guardián del barrio", "20 reportes en tu zona", false),
            Badge("b7", "Racha de 7 días", "Reporta 7 días seguidos", false),
            Badge("b8", "Precisión total", "100% reportes verificados", false),
            Badge("b9", "Vigilante", "50 reportes publicados", false)
        )
    )

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            _user.value = authRepository.getUserByEmail(email)
        }
    }
}
