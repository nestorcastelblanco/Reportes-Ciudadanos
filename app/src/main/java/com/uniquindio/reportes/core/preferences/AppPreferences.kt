package com.uniquindio.reportes.core.preferences

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SortOption { MAS_RECIENTE, MAS_CERCANO, MAS_RELEVANTE }

/**
 * Preferencias en memoria, compartidas entre Home y MainActivity (tema).
 * Persistencia (DataStore) puede agregarse después sin cambiar la API.
 */
@Singleton
class AppPreferences @Inject constructor() {

    private val _darkMode = MutableStateFlow<Boolean?>(null) // null = sistema
    val darkMode: StateFlow<Boolean?> = _darkMode.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.MAS_RECIENTE)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

    /** Radio en km. null = sin límite. */
    private val _radiusKm = MutableStateFlow<Double?>(1.0)
    val radiusKm: StateFlow<Double?> = _radiusKm.asStateFlow()

    fun setDarkMode(value: Boolean?) { _darkMode.value = value }
    fun setSortBy(value: SortOption) { _sortBy.value = value }
    fun setVerifiedOnly(value: Boolean) { _verifiedOnly.value = value }
    fun setRadiusKm(value: Double?) { _radiusKm.value = value }
}
