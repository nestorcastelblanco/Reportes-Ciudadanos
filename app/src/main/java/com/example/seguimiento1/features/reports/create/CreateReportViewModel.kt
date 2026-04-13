package com.example.seguimiento1.features.reports.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CreateReportData
import com.example.seguimiento1.domain.model.ReportCategory
import com.example.seguimiento1.domain.repository.ReportRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _category = MutableStateFlow(ReportCategory.SECURITY)
    val category: StateFlow<ReportCategory> = _category.asStateFlow()

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onAddressChange(value: String) {
        _address.value = value
    }

    fun onCategoryChange(value: ReportCategory) {
        _category.value = value
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            reportRepository.createReport(
                CreateReportData(
                    title = title.value,
                    description = description.value,
                    address = address.value,
                    category = category.value,
                    reporterEmail = email
                )
            )
            onSuccess()
        }
    }
}

