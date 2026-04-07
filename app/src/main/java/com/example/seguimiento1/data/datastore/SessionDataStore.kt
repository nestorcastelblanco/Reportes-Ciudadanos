package com.example.seguimiento1.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

class SessionDataStore(private val context: Context) {

    private object Keys {
        val isLoggedIn: Preferences.Key<Boolean> = booleanPreferencesKey("is_logged_in")
        val email: Preferences.Key<String> = stringPreferencesKey("session_email")
    }

    val isLoggedInFlow: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.isLoggedIn] ?: false
    }

    val emailFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[Keys.email]
    }

    suspend fun saveSession(email: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.isLoggedIn] = true
            prefs[Keys.email] = email
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.isLoggedIn] = false
            prefs.remove(Keys.email)
        }
    }
}

