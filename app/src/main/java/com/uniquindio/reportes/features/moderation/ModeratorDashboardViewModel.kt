package com.uniquindio.reportes.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ModerationStats(
    val pending: Int = 0,
    val verified: Int = 0,
    val rejected: Int = 0,
    val resolved: Int = 0,
    val total: Int = 0
)

@HiltViewModel
class ModeratorDashboardViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val stats: StateFlow<ModerationStats> = reportRepository.reportsFlow
        .map { reports ->
            ModerationStats(
                pending = reports.count { it.status == ReportStatus.PENDING },
                verified = reports.count { it.status == ReportStatus.VERIFIED },
                rejected = reports.count { it.status == ReportStatus.REJECTED },
                resolved = reports.count { it.status == ReportStatus.RESOLVED },
                total = reports.size
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ModerationStats()
        )
}
