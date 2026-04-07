package com.example.seguimiento1.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.usersDataStore by preferencesDataStore(name = "users_prefs")

class UsersDataStore(private val context: Context) {

    private object Keys {
        val usersJson: Preferences.Key<String> = stringPreferencesKey("users_json")
    }

    val usersFlow: Flow<Map<String, String>> = context.usersDataStore.data.map { prefs ->
        decodeUsers(prefs[Keys.usersJson])
    }

    suspend fun saveUsers(users: Map<String, String>) {
        context.usersDataStore.edit { prefs ->
            prefs[Keys.usersJson] = encodeUsers(users)
        }
    }

    private fun decodeUsers(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) {
            return mapOf("demo@ciudad.com" to "demo1234")
        }

        return try {
            val jsonObject = JSONObject(raw)
            buildMap {
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, jsonObject.optString(key))
                }
            }
        } catch (_: Exception) {
            mapOf("demo@ciudad.com" to "demo1234")
        }
    }

    private fun encodeUsers(users: Map<String, String>): String {
        return JSONObject(users).toString()
    }
}

