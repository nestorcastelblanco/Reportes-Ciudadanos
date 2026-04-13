package com.example.seguimiento1.features.reports.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seguimiento1.domain.model.CreateReportData
import com.example.seguimiento1.domain.model.ReportCategory
import com.example.seguimiento1.domain.repository.AuthRepository
import com.example.seguimiento1.domain.repository.ReportRepository
import com.example.seguimiento1.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

typealias GeocodeFn = suspend (String) -> Pair<Double, Double>?

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository
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

    fun suggestCategoryFromText(): ReportCategory {
        val text = "${title.value} ${description.value}".lowercase()
        val suggested = when {
            text.containsAny("robo", "hurto", "ladron", "arma", "inseguridad", "atraco") -> ReportCategory.SECURITY
            text.containsAny("accidente", "ambulancia", "herido", "sangre", "emergencia", "medico") -> ReportCategory.MEDICAL_EMERGENCIES
            text.containsAny("hueco", "poste", "alcantarilla", "fuga", "basura", "alumbrado", "via") -> ReportCategory.INFRASTRUCTURE
            text.containsAny("perro", "gato", "mascota", "animal") -> ReportCategory.PETS
            else -> ReportCategory.COMMUNITY
        }
        _category.value = suggested
        return suggested
    }

    fun submit(
        imageUrl: String?,
        latitude: Double?,
        longitude: Double?,
        geocode: GeocodeFn,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            val (lat, lng) = if (latitude != null && longitude != null) {
                latitude to longitude
            } else if (address.value.isNotBlank()) {
                geocode(address.value) ?: (null to null)
            } else {
                null to null
            }
            reportRepository.createReport(
                CreateReportData(
                    title = title.value,
                    description = description.value,
                    address = address.value,
                    category = category.value,
                    reporterEmail = email,
                    imageUrl = imageUrl,
                    latitude = lat,
                    longitude = lng
                )
            )
            authRepository.addPoints(email, 10)
            onSuccess()
        }
    }
}

private fun String.containsAny(vararg tokens: String): Boolean {
    return tokens.any { contains(it) }
}

