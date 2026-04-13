package com.example.seguimiento1.features.moderation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CitizenReport
import com.example.seguimiento1.domain.model.User
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val reportId: String = savedStateHandle["reportId"] ?: ""

    private val _report = MutableStateFlow<CitizenReport?>(null)
    val report = _report.asStateFlow()

    private val _reporter = MutableStateFlow<User?>(null)
    val reporter = _reporter.asStateFlow()

    private val _reporterTotalReports = MutableStateFlow(0)
    val reporterTotalReports = _reporterTotalReports.asStateFlow()

    private val _reporterVerifiedReports = MutableStateFlow(0)
    val reporterVerifiedReports = _reporterVerifiedReports.asStateFlow()

    private val _rejectReason = MutableStateFlow("")
    val rejectReason = _rejectReason.asStateFlow()

    private val _actionDone = MutableStateFlow(false)
    val actionDone = _actionDone.asStateFlow()

    init {
        viewModelScope.launch {
            val reports = reportRepository.reportsFlow.first()
            val r = reports.find { it.id == reportId }
            _report.value = r
            if (r != null) {
                _reporter.value = authRepository.getUserByEmail(r.reporterEmail)
                val userReports = reportRepository.reportsByEmail(r.reporterEmail).first()
                _reporterTotalReports.value = userReports.size
                _reporterVerifiedReports.value = userReports.count {
                    it.status == com.example.seguimiento1.domain.model.ReportStatus.VERIFIED ||
                            it.status == com.example.seguimiento1.domain.model.ReportStatus.RESOLVED
                }
            }
        }
    }

    fun onRejectReasonChange(r: String) { _rejectReason.value = r }

    fun verify() {
        viewModelScope.launch {
            reportRepository.verifyReport(reportId)
            _actionDone.value = true
        }
    }

    fun reject() {
        viewModelScope.launch {
            reportRepository.rejectReport(reportId)
            _actionDone.value = true
        }
    }

    fun markResolved() {
        viewModelScope.launch {
            reportRepository.markResolved(reportId)
            _actionDone.value = true
        }
    }
}
