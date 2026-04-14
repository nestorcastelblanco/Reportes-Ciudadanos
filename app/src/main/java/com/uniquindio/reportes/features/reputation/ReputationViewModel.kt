package com.uniquindio.reportes.features.reputation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.Badge
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
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
            Badge("b1", R.string.badge_first_publication, R.string.badge_desc_first_publication, true),
            Badge("b2", R.string.badge_10_verified, R.string.badge_desc_10_verified, true),
            Badge("b3", R.string.badge_featured_month, R.string.badge_desc_featured_month, true),
            Badge("b4", R.string.badge_50_comments, R.string.badge_desc_50_comments, true),
            Badge("b5", R.string.badge_100_votes, R.string.badge_desc_100_votes, true),
            Badge("b6", R.string.badge_neighborhood_guardian, R.string.badge_desc_neighborhood_guardian, false),
            Badge("b7", R.string.badge_7_day_streak, R.string.badge_desc_7_day_streak, false),
            Badge("b8", R.string.badge_total_precision, R.string.badge_desc_total_precision, false),
            Badge("b9", R.string.badge_vigilante, R.string.badge_desc_vigilante, false)
        )
    )

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            _user.value = authRepository.getUserByEmail(email)
        }
    }
}
