package com.example.checksumx.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.checksumx.data.model.AppTheme
import com.example.checksumx.data.model.ChecksumAlgorithm
import com.example.checksumx.data.model.OperationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checksumx_preferences")

data class UserPreferences(
    val theme: AppTheme = AppTheme.SYSTEM,
    val defaultAlgorithm: ChecksumAlgorithm = ChecksumAlgorithm.SHA256,
    val defaultOperation: OperationMode = OperationMode.GENERATE,
    val uppercaseHex: Boolean = false,
    val clearOnExit: Boolean = false,
    val hapticFeedback: Boolean = true
)

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val DEFAULT_ALGORITHM = stringPreferencesKey("default_algorithm")
        val DEFAULT_OPERATION = stringPreferencesKey("default_operation")
        val UPPERCASE_HEX = booleanPreferencesKey("uppercase_hex")
        val CLEAR_ON_EXIT = booleanPreferencesKey("clear_on_exit")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
            val theme = runCatching { AppTheme.valueOf(themeStr) }.getOrDefault(AppTheme.SYSTEM)

            val algoStr = preferences[PreferencesKeys.DEFAULT_ALGORITHM] ?: ChecksumAlgorithm.SHA256.name
            val algorithm = runCatching { ChecksumAlgorithm.valueOf(algoStr) }.getOrDefault(ChecksumAlgorithm.SHA256)

            val opStr = preferences[PreferencesKeys.DEFAULT_OPERATION] ?: OperationMode.GENERATE.name
            val operation = runCatching { OperationMode.valueOf(opStr) }.getOrDefault(OperationMode.GENERATE)

            val uppercaseHex = preferences[PreferencesKeys.UPPERCASE_HEX] ?: false
            val clearOnExit = preferences[PreferencesKeys.CLEAR_ON_EXIT] ?: false
            val hapticFeedback = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true

            UserPreferences(
                theme = theme,
                defaultAlgorithm = algorithm,
                defaultOperation = operation,
                uppercaseHex = uppercaseHex,
                clearOnExit = clearOnExit,
                hapticFeedback = hapticFeedback
            )
        }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    suspend fun setDefaultAlgorithm(algorithm: ChecksumAlgorithm) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ALGORITHM] = algorithm.name
        }
    }

    suspend fun setDefaultOperation(operation: OperationMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_OPERATION] = operation.name
        }
    }

    suspend fun setUppercaseHex(uppercase: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPPERCASE_HEX] = uppercase
        }
    }

    suspend fun setClearOnExit(clear: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLEAR_ON_EXIT] = clear
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
