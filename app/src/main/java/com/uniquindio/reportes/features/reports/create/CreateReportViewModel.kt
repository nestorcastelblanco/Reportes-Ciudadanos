package com.uniquindio.reportes.features.reports.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniquindio.reportes.data.location.LocationProvider
import com.uniquindio.reportes.data.location.UserLocation
import com.uniquindio.reportes.domain.model.CreateReportData
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.CategoryClassifier
import com.uniquindio.reportes.domain.repository.ImageStorageRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

typealias GeocodeFn = suspend (String) -> Pair<Double, Double>?

@OptIn(FlowPreview::class)
@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val categoryClassifier: CategoryClassifier,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _category = MutableStateFlow(ReportCategory.SECURITY)
    val category: StateFlow<ReportCategory> = _category.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isClassifying = MutableStateFlow(false)
    val isClassifying: StateFlow<Boolean> = _isClassifying.asStateFlow()

    /** El usuario eligió categoría manualmente: dejar de auto-sugerir. */
    private val categoryLockedByUser = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            _userLocation.value = locationProvider.currentLocation()
        }
        combine(_title, _description) { t, d -> t to d }
            .debounce(AUTO_CLASSIFY_DEBOUNCE_MS)
            .distinctUntilChanged()
            .filter { (t, d) ->
                !categoryLockedByUser.value &&
                    (t.length >= MIN_CHARS_TO_CLASSIFY || d.length >= MIN_CHARS_TO_CLASSIFY)
            }
            .onEach { (t, d) -> autoClassify(t, d) }
            .launchIn(viewModelScope)
    }

    private suspend fun autoClassify(title: String, description: String) {
        _isClassifying.value = true
        try {
            val suggested = categoryClassifier.classify(title, description)
                ?: heuristicCategory("$title $description")
            if (!categoryLockedByUser.value) {
                _category.value = suggested
            }
        } finally {
            _isClassifying.value = false
        }
    }

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onAddressChange(value: String) {
        _address.value = value
    }

    fun fetchCurrentLocation(onResult: (UserLocation?) -> Unit) {
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            _userLocation.value = location
            onResult(location)
        }
    }

    fun onCategoryChange(value: ReportCategory) {
        categoryLockedByUser.value = true
        _category.value = value
    }

    /** Forzar una clasificación con Gemini al pulsar el botón "sugerir". */
    fun suggestCategoryFromText(onResult: (ReportCategory) -> Unit) {
        viewModelScope.launch {
            _isClassifying.value = true
            try {
                val suggested = categoryClassifier.classify(title.value, description.value)
                    ?: heuristicCategory("${title.value} ${description.value}")
                categoryLockedByUser.value = false
                _category.value = suggested
                onResult(suggested)
            } finally {
                _isClassifying.value = false
            }
        }
    }

    private fun heuristicCategory(raw: String): ReportCategory {
        val text = raw.lowercase()
        return when {
            text.containsAny("robo", "hurto", "ladron", "arma", "inseguridad", "atraco") -> ReportCategory.SECURITY
            text.containsAny("accidente", "ambulancia", "herido", "sangre", "emergencia", "medico") -> ReportCategory.MEDICAL_EMERGENCIES
            text.containsAny("hueco", "poste", "alcantarilla", "fuga", "basura", "alumbrado", "via") -> ReportCategory.INFRASTRUCTURE
            text.containsAny("perro", "gato", "mascota", "animal") -> ReportCategory.PETS
            else -> ReportCategory.COMMUNITY
        }
    }

    private companion object {
        const val AUTO_CLASSIFY_DEBOUNCE_MS = 900L
        const val MIN_CHARS_TO_CLASSIFY = 12
    }

    fun submit(
        imageUrls: List<String>,
        latitude: Double?,
        longitude: Double?,
        geocode: GeocodeFn,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val email = sessionRepository.sessionFlow.first().email.orEmpty()
                val user = authRepository.getUserByEmail(email)
                val (lat, lng) = if (latitude != null && longitude != null) {
                    latitude to longitude
                } else if (address.value.isNotBlank()) {
                    geocode(address.value) ?: (null to null)
                } else {
                    null to null
                }
                val uploadedUrls = if (imageUrls.isEmpty()) {
                    emptyList()
                } else {
                    runCatching {
                        imageStorageRepository.uploadReportImages(imageUrls, email)
                    }.onFailure { e ->
                        android.util.Log.e(
                            "CreateReportVM",
                            "image upload failed (${imageUrls.size} imgs): ${e::class.java.simpleName}: ${e.message}",
                            e
                        )
                    }.getOrDefault(emptyList())
                }
                android.util.Log.d(
                    "CreateReportVM",
                    "createReport: input=${imageUrls.size} imgs, uploaded=${uploadedUrls.size}"
                )
                reportRepository.createReport(
                    CreateReportData(
                        title = title.value,
                        description = description.value,
                        address = address.value,
                        category = category.value,
                        reporterEmail = email,
                        reporterName = user?.nombre.orEmpty(),
                        imageUrls = uploadedUrls,
                        latitude = lat,
                        longitude = lng
                    )
                )
                authRepository.addPoints(email, 10)
                onSuccess()
            } finally {
                _isUploading.value = false
            }
        }
    }
}

private fun String.containsAny(vararg tokens: String): Boolean {
    return tokens.any { contains(it) }
}

