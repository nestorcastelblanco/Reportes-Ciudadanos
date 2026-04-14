package com.uniquindio.reportes.features.reports.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.uniquindio.reportes.core.navigation.EditReportRoute
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

@HiltViewModel
class EditReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val reportId: String = try {
        savedStateHandle.toRoute<EditReportRoute>().reportId
    } catch (_: SerializationException) { "" }

    private val _report = MutableStateFlow<CitizenReport?>(null)
    val report: StateFlow<CitizenReport?> = _report.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _category = MutableStateFlow(ReportCategory.SECURITY)
    val category: StateFlow<ReportCategory> = _category.asStateFlow()

    init {
        viewModelScope.launch {
            val r = reportRepository.reportById(reportId).first()
            r?.let {
                _report.value = it
                _title.value = it.title
                _description.value = it.description
                _category.value = it.category
            }
        }
    }

    fun onTitleChange(v: String) { _title.value = v }
    fun onDescriptionChange(v: String) { _description.value = v }
    fun onCategoryChange(v: ReportCategory) { _category.value = v }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val r = _report.value ?: return@launch
            reportRepository.updateReport(
                r.copy(title = _title.value, description = _description.value, category = _category.value)
            )
            onSuccess()
        }
    }

    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            reportRepository.deleteReport(reportId)
            onSuccess()
        }
    }
}
