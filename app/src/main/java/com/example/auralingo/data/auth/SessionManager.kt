package com.example.auralingo.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "auralingo_session")

/**
 * Manages active user session persistence using Jetpack DataStore Preferences.
 */
class SessionManager private constructor(context: Context) {

    private val dataStore: DataStore<Preferences> = context.applicationContext.sessionDataStore
    private val KEY_CURRENT_USER_ID = longPreferencesKey("current_user_id")

    /**
     * Reactive stream of the currently logged in user ID (emits null when no active session).
     */
    val currentUserId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_USER_ID]
    }

    /**
     * Stores the active user ID in DataStore.
     */
    suspend fun setCurrentUserId(userId: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_USER_ID] = userId
        }
    }

    /**
     * Clears the current user session ID from DataStore.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_CURRENT_USER_ID)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        /**
         * Returns singleton instance of SessionManager.
         */
        fun getInstance(context: Context): SessionManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
    }
}
