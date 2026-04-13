package com.example.seguimiento1.features.reports.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.seguimiento1.core.navigation.ReportDetailRoute
import com.example.seguimiento1.domain.model.CitizenReport
import com.example.seguimiento1.domain.model.Comment
import com.example.seguimiento1.domain.model.User
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.CommentRepository
import com.example.seguimiento1.domain.repository.ReportRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.util.UUID

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val commentRepository: CommentRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val reportId: String = try {
        savedStateHandle.toRoute<ReportDetailRoute>().reportId
    } catch (_: SerializationException) {
        ""
    }

    val report: StateFlow<CitizenReport?> = reportRepository.reportById(reportId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    val comments: StateFlow<List<Comment>> = commentRepository.commentsForReport(reportId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentEmail = MutableStateFlow("")
    val currentEmail: StateFlow<String> = _currentEmail.asStateFlow()

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            _currentEmail.value = email
            _currentUser.value = authRepository.getUserByEmail(email)
        }
    }

    fun onCommentTextChange(value: String) {
        _commentText.value = value
    }

    fun submitComment() {
        viewModelScope.launch {
            val email = _currentEmail.value
            val user = _currentUser.value ?: return@launch
            val text = _commentText.value.trim()
            if (text.isBlank()) return@launch

            commentRepository.addComment(
                Comment(
                    id = UUID.randomUUID().toString(),
                    reportId = reportId,
                    authorEmail = email,
                    authorName = user.nombre,
                    text = text,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
            authRepository.addPoints(email, 3)
            _commentText.value = ""
        }
    }

    fun toggleImportance() {
        viewModelScope.launch {
            val email = _currentEmail.value
            reportRepository.toggleImportance(reportId, email)
        }
    }

    fun isOwnReport(): Boolean {
        return report.value?.reporterEmail == _currentEmail.value
    }
}


