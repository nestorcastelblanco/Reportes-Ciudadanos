package com.example.seguimiento1.features.statistics

import androidx.lifecycle.ViewModel
import com.example.seguimiento1.domain.model.ReportCategory
import com.example.seguimiento1.domain.model.ReportStatus
import com.example.seguimiento1.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    reportRepository: ReportRepository
) : ViewModel() {

    val state: Flow<StatsUiState> = reportRepository.reportsFlow.map { reports ->
        val active = reports.count { it.status == ReportStatus.VERIFIED }
        val finished = reports.count { it.status == ReportStatus.RESOLVED }
        val pending = reports.count { it.status == ReportStatus.PENDING }

        val byCategory = ReportCategory.entries.map { cat ->
            CategoryStat(cat, reports.count { r -> r.category == cat })
        }.sortedByDescending { it.count }

        // Monthly activity: group by month from creation timestamp
        val calendar = java.util.Calendar.getInstance()
        val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val monthlyMap = mutableMapOf<String, Int>()
        reports.forEach { report ->
            calendar.timeInMillis = report.createdAtMillis
            val key = monthNames[calendar.get(java.util.Calendar.MONTH)]
            monthlyMap[key] = (monthlyMap[key] ?: 0) + 1
        }
        // Show last 6 months
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
