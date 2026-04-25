package com.example.planer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.planer.security.BiometricManager

class BiometricSettingsViewModelFactory(
    private val biometricManager: BiometricManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BiometricSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BiometricSettingsViewModel(biometricManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}