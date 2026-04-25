package com.example.planer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.planer.security.BiometricManager
import com.example.planer.security.BiometricAuthState
import com.example.planer.security.BiometricType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BiometricSettingsViewModel(
    private val biometricManager: BiometricManager
) : ViewModel() {

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _biometricType = MutableStateFlow(BiometricType.NONE)
    val biometricType: StateFlow<BiometricType> = _biometricType.asStateFlow()

    private val _authState = MutableStateFlow<BiometricAuthState>(BiometricAuthState.Idle)
    val authState: StateFlow<BiometricAuthState> = _authState.asStateFlow()

    private val _biometricTypeName = MutableStateFlow("")
    val biometricTypeName: StateFlow<String> = _biometricTypeName.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            biometricManager.isEnabledByUser().collect { enabled ->
                _isBiometricEnabled.value = enabled
            }
        }

        _biometricType.value = biometricManager.checkAvailability()
        _biometricTypeName.value = biometricManager.getBiometricTypeName()
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            biometricManager.setEnabled(enabled)
            _isBiometricEnabled.value = enabled
        }
    }

    fun testAuthentication(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        if (!_isBiometricEnabled.value) {
            onFailure.invoke()
            return
        }

        biometricManager.authenticate(
            reason = "Підтвердіть особу для доступу до захищених функцій",
            onSuccess = {
                _authState.value = BiometricAuthState.Success
                onSuccess.invoke()
                resetAuthState()
            },
            onFailure = {
                onFailure.invoke()
                resetAuthState()
            }
        )

        viewModelScope.launch {
            biometricManager.authenticationState.collect { state ->
                _authState.value = state
            }
        }
    }

    private fun resetAuthState() {
        viewModelScope.launch {
            delay(2000)
            _authState.value = BiometricAuthState.Idle
        }
    }

    fun getBiometricIcon(): String {
        return when (_biometricType.value) {
            BiometricType.FINGERPRINT -> "👆"
            BiometricType.FACE_RECOGNITION -> "😀"
            BiometricType.IRIS -> "👁"
            BiometricType.NONE -> "❌"
        }
    }
}