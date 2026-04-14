package com.uniquindio.reportes.features.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.R
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import com.uniquindio.reportes.core.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryStat(val category: ReportCategory, val count: Int)

data class StatsUiState(
    val active: Int = 0,
    val finished: Int = 0,
    val pending: Int = 0,
    val totalReports: Int = 0,
    val byCategory: List<CategoryStat> = emptyList(),
    val monthlyActivity: List<Pair<String, Int>> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    reportRepository: ReportRepository,
    sessionRepository: SessionRepository,
    resourceProvider: ResourceProvider
) : ViewModel() {

    private val monthNameResIds = intArrayOf(
        R.string.month_jan, R.string.month_feb, R.string.month_mar,
        R.string.month_apr, R.string.month_may, R.string.month_jun,
        R.string.month_jul, R.string.month_aug, R.string.month_sep,
        R.string.month_oct, R.string.month_nov, R.string.month_dec
    )

    private val _email = MutableStateFlow("")

    init {
        viewModelScope.launch {
            sessionRepository.sessionFlow.collect { session ->
                _email.value = session.email.orEmpty()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<StatsUiState> = _email.flatMapLatest { email ->
        reportRepository.reportsByEmail(email).map { reports ->
            val active = reports.count { it.status == ReportStatus.VERIFIED }
            val finished = reports.count { it.status == ReportStatus.RESOLVED }
            val pending = reports.count { it.status == ReportStatus.PENDING }

            val byCategory = ReportCategory.entries.map { cat ->
                CategoryStat(cat, reports.count { r -> r.category == cat })
            }.sortedByDescending { it.count }

            val calendar = java.util.Calendar.getInstance()
            val monthNames = monthNameResIds.map { resourceProvider.getString(it) }.toTypedArray()
            val monthlyMap = mutableMapOf<String, Int>()
            reports.forEach { report ->
                calendar.timeInMillis = report.createdAtMillis
                val key = monthNames[calendar.get(java.util.Calendar.MONTH)]
                monthlyMap[key] = (monthlyMap[key] ?: 0) + 1
            }
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
            val monthly = (0..5).reversed().map { offset ->
                val idx = (currentMonth - offset + 12) % 12
                val name = monthNames[idx]
                name to (monthlyMap[name] ?: 0)
            }

            StatsUiState(
                active = active,
                finished = finished,
                pending = pending,
                totalReports = reports.size,
                byCategory = byCategory,
                monthlyActivity = monthly
            )
        }
    }
}
