package com.uniquindio.reportes.features.my_reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    reportRepository: ReportRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val reports: StateFlow<List<CitizenReport>> = sessionRepository.sessionFlow
        .map { it.email.orEmpty() }
        .flatMapLatest { email -> reportRepository.reportsByEmail(email) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
