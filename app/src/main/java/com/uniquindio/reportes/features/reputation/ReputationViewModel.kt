package com.uniquindio.reportes.features.reputation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.Badge
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.model.User
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.CommentRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ReputationViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _email = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            _email.value = email
            _user.value = authRepository.getUserByEmail(email)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val badges: StateFlow<List<Badge>> = _email.flatMapLatest { email ->
        combine(
            reportRepository.reportsByEmail(email),
            commentRepository.commentsFlow
        ) { reports, allComments ->
            val verifiedCount = reports.count { it.status == ReportStatus.VERIFIED }
            val totalVotesReceived = reports.sumOf { it.voterEmails.size }
            val userCommentCount = allComments.count { it.authorEmail == email }
            val allVerified = reports.isNotEmpty() && reports.all { it.status == ReportStatus.VERIFIED }
            val featuredReport = reports.any { it.importance >= 50 }

            listOf(
                Badge("b1", R.string.badge_first_publication, R.string.badge_desc_first_publication,
                    reports.isNotEmpty()),
                Badge("b2", R.string.badge_10_verified, R.string.badge_desc_10_verified,
                    verifiedCount >= 10),
                Badge("b3", R.string.badge_featured_month, R.string.badge_desc_featured_month,
                    featuredReport),
                Badge("b4", R.string.badge_50_comments, R.string.badge_desc_50_comments,
                    userCommentCount >= 50),
                Badge("b5", R.string.badge_100_votes, R.string.badge_desc_100_votes,
                    totalVotesReceived >= 100),
                Badge("b6", R.string.badge_neighborhood_guardian, R.string.badge_desc_neighborhood_guardian,
                    reports.size >= 20),
                Badge("b7", R.string.badge_7_day_streak, R.string.badge_desc_7_day_streak,
                    false),
                Badge("b8", R.string.badge_total_precision, R.string.badge_desc_total_precision,
                    allVerified),
                Badge("b9", R.string.badge_vigilante, R.string.badge_desc_vigilante,
                    reports.size >= 50)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
