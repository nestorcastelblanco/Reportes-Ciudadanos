package com.example.seguimiento1.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CitizenReport
import com.example.seguimiento1.domain.model.ReportStatus
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ModerationTab { PENDING, VERIFIED, REJECTED }

@HiltViewModel
class ModerationPanelViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(ModerationTab.PENDING)
    val selectedTab = _selectedTab.asStateFlow()

    val filteredReports: kotlinx.coroutines.flow.Flow<List<CitizenReport>> = combine(reportRepository.reportsFlow, _selectedTab) { reports, tab ->
        val status = when (tab) {
            ModerationTab.PENDING -> ReportStatus.PENDING
            ModerationTab.VERIFIED -> ReportStatus.VERIFIED
            ModerationTab.REJECTED -> ReportStatus.REJECTED
        }
        reports.filter { it.status == status }
    }

    val stats: kotlinx.coroutines.flow.Flow<Triple<Int, Int, Int>> = reportRepository.reportsFlow.combine(_selectedTab) { reports, _ ->
        Triple(
            reports.count { it.status == ReportStatus.PENDING },
            reports.count { it.status == ReportStatus.VERIFIED },
            reports.count { it.status == ReportStatus.REJECTED }
        )
    }

    fun selectTab(tab: ModerationTab) {
        _selectedTab.value = tab
    }

    fun verifyReport(reportId: String) {
        viewModelScope.launch { reportRepository.verifyReport(reportId) }
    }

    fun rejectReport(reportId: String) {
        viewModelScope.launch { reportRepository.rejectReport(reportId) }
    }
}
