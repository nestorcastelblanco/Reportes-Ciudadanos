package com.uniquindio.reportes.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    reportRepository: ReportRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val reports: StateFlow<List<CitizenReport>> = reportRepository.reportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }
}
