package com.uniquindio.reportes.core.preferences

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = prefs.darkMode
    val sortBy: StateFlow<SortOption> = prefs.sortBy
    val verifiedOnly: StateFlow<Boolean> = prefs.verifiedOnly

    fun setDarkMode(value: Boolean?) = prefs.setDarkMode(value)
    fun setSortBy(value: SortOption) = prefs.setSortBy(value)
    fun setVerifiedOnly(value: Boolean) = prefs.setVerifiedOnly(value)
}
