package com.example.seguimiento1.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CitizenReport
import com.example.seguimiento1.domain.model.ReportCategory
import com.example.seguimiento1.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ReportCategory?>(null)

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

