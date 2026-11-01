package com.example.checksumx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.checksumx.data.model.AppTheme
import com.example.checksumx.data.model.ChecksumAlgorithm
import com.example.checksumx.data.model.OperationMode
import com.example.checksumx.data.preferences.PreferencesRepository
import com.example.checksumx.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }

    fun setDefaultAlgorithm(algorithm: ChecksumAlgorithm) {
        viewModelScope.launch {
            preferencesRepository.setDefaultAlgorithm(algorithm)
        }
    }

    fun setDefaultOperation(operation: OperationMode) {
        viewModelScope.launch {
            preferencesRepository.setDefaultOperation(operation)
        }
    }

    fun setUppercaseHex(uppercase: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUppercaseHex(uppercase)
        }
    }

    fun setClearOnExit(clear: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setClearOnExit(clear)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedback(enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
        }
    }

    class Factory(private val preferencesRepository: PreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
