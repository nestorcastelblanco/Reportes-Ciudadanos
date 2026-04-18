package com.uniquindio.reportes.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.repository.ReportRepository
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

    val searchQuery = MutableStateFlow("")

    private val _selectedCategory = MutableStateFlow<ReportCategory?>(null)
    val selectedCategory: StateFlow<ReportCategory?> = _selectedCategory

    val reports: StateFlow<List<CitizenReport>> = combine(
        reportRepository.reportsFlow,
        _selectedCategory
    ) { allReports, category ->
        if (category == null) allReports
        else allReports.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilter(category: ReportCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}
