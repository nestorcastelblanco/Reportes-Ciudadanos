package com.uniquindio.reportes.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.uniquindio.reportes.core.preferences.AppPreferences
import com.uniquindio.reportes.core.preferences.SortOption
import com.uniquindio.reportes.data.location.LocationProvider
import com.uniquindio.reportes.data.location.UserLocation
import com.uniquindio.reportes.data.location.reverseGeocode
import dagger.hilt.android.qualifiers.ApplicationContext
import com.uniquindio.reportes.domain.model.CitizenReport
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.model.ReportStatus
import com.uniquindio.reportes.domain.repository.AuthRepository
import com.uniquindio.reportes.domain.repository.ReportRepository
import com.uniquindio.reportes.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class HomeViewModel @Inject constructor(
    reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val locationProvider: LocationProvider,
    private val preferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val radiusKm: StateFlow<Double?> = preferences.radiusKm
    val sortBy: StateFlow<SortOption> = preferences.sortBy
    val verifiedOnly: StateFlow<Boolean> = preferences.verifiedOnly

    fun setRadiusKm(value: Double?) = preferences.setRadiusKm(value)

    private val _cityLabel = MutableStateFlow("")
    val cityLabel: StateFlow<String> = _cityLabel.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ReportCategory?>(null)

    private val _userInitials = MutableStateFlow("")
    val userInitials: StateFlow<String> = _userInitials.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocation?>(null)

    init {
        viewModelScope.launch {
            val email = sessionRepository.sessionFlow.first().email.orEmpty()
            val user = authRepository.getUserByEmail(email)
            _userInitials.value = user?.initials ?: ""
        }
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            _userLocation.value = location
            if (location != null) {
                _cityLabel.value = context.reverseGeocode(location.latitude, location.longitude)
                    ?.cityLabel.orEmpty()
            }
        }
    }

    private val queryCategoryLocation = combine(
        searchQuery, selectedCategory, _userLocation
    ) { q, c, l -> Triple(q, c, l) }

    private val prefsCombined = combine(
        preferences.radiusKm, preferences.sortBy, preferences.verifiedOnly
    ) { r, s, v -> Triple(r, s, v) }

    private val filters = combine(queryCategoryLocation, prefsCombined) { qcl, prefs ->
        FilterState(
            query = qcl.first,
            category = qcl.second,
            location = qcl.third,
            radiusKm = prefs.first,
            sortBy = prefs.second,
            verifiedOnly = prefs.third
        )
    }

    val reports: StateFlow<List<CitizenReport>> = combine(
        reportRepository.reportsFlow,
        filters
    ) { allReports, f ->
        val basics = allReports.filter { report ->
            report.status != ReportStatus.REJECTED &&
                (f.category == null || report.category == f.category) &&
                (!f.verifiedOnly || report.status == ReportStatus.VERIFIED) &&
                (f.query.isBlank() || report.title.contains(f.query, ignoreCase = true) ||
                        report.description.contains(f.query, ignoreCase = true))
        }

        val withinRadius = if (f.location != null && f.radiusKm != null) {
            val limit = f.radiusKm * 1000.0
            basics.filter { r ->
                if (r.latitude == null || r.longitude == null) return@filter false
                haversineMeters(
                    f.location.latitude, f.location.longitude,
                    r.latitude, r.longitude
                ) <= limit
            }
        } else basics

        when (f.sortBy) {
            SortOption.MAS_RECIENTE -> withinRadius.sortedByDescending { it.createdAtMillis }
            SortOption.MAS_CERCANO -> if (f.location != null) {
                withinRadius.sortedBy { r ->
                    if (r.latitude != null && r.longitude != null) {
                        haversineMeters(
                            f.location.latitude, f.location.longitude,
                            r.latitude, r.longitude
                        )
                    } else Double.MAX_VALUE
                }
            } else withinRadius
            SortOption.MAS_RELEVANTE -> withinRadius.sortedByDescending { it.importance }
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

    fun refreshUserLocation() {
        viewModelScope.launch {
            val location = locationProvider.currentLocation()
            _userLocation.value = location
            if (location != null) {
                _cityLabel.value = context.reverseGeocode(location.latitude, location.longitude)
                    ?.cityLabel.orEmpty()
            }
        }
    }
}

private data class FilterState(
    val query: String,
    val category: ReportCategory?,
    val location: UserLocation?,
    val radiusKm: Double?,
    val sortBy: SortOption,
    val verifiedOnly: Boolean
)

private const val EARTH_RADIUS_METERS = 6_371_000.0

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_METERS * c
}
