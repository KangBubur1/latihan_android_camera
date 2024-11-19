package com.example.subawal_inter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreManager private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val TOKEN_KEY = stringPreferencesKey("token")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Function to get token
    fun getToken(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[TOKEN_KEY]
            }
    }

    suspend fun clearToken() {
        try {
            context.dataStore.edit { preferences -> preferences.remove(TOKEN_KEY) }
        } catch (e: Exception) {
            throw e
        }
    }

    companion object {
        @Volatile
        private var instance: DataStoreManager? = null

        // Singleton pattern to ensure only one instance is used
        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
