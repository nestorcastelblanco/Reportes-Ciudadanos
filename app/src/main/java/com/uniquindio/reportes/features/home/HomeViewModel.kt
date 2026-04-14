package com.uniquindio.reportes.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ReportCategory?>(null)

    private val _userInitials = MutableStateFlow("")
    val userInitials: StateFlow<String> = _userInitials.asStateFlow()

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            val user = authRepository.getUserByEmail(email)
            _userInitials.value = user?.initials ?: ""
        }
    }

    val reports: StateFlow<List<CitizenReport>> = combine(
        reportRepository.reportsFlow,
        searchQuery,
        selectedCategory
    ) { allReports, query, category ->
        allReports
            .filter { report ->
                (category == null || report.category == category) &&
                (query.isBlank() || report.title.contains(query, ignoreCase = true) ||
                        report.description.contains(query, ignoreCase = true))
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilter(category: ReportCategory?) {
        selectedCategory.value = category
    }
}

