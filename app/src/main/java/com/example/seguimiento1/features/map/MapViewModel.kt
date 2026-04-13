package com.example.seguimiento1.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CitizenReport
import com.example.seguimiento1.domain.model.ReportCategory
import com.example.seguimiento1.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val selectedCategory = MutableStateFlow<ReportCategory?>(null)
    val searchQuery = MutableStateFlow("")

    val reports: StateFlow<List<CitizenReport>> = combine(
        reportRepository.reportsFlow,
        selectedCategory,
        searchQuery
    ) { all, category, query ->
        all.filter { report ->
            (category == null || report.category == category) &&
            (query.isBlank() || report.title.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onCategoryFilter(category: ReportCategory?) {
        selectedCategory.value = category
    }

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }
}
