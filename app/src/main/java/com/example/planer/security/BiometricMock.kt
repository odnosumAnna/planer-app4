package com.example.planer.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BiometricMock {

    private val authState = MutableStateFlow<BiometricAuthState>(BiometricAuthState.Idle)
    val authenticationState: StateFlow<BiometricAuthState> = authState

    private var mockEnabled = false
    private var mockShouldSucceed = true
    private var mockSensorAvailable = true

    fun checkAvailability(): BiometricType {
        return if (mockSensorAvailable) {
            BiometricType.FINGERPRINT
        } else {
            BiometricType.NONE
        }
    }

    fun isEnabledByUser(): Boolean {
        return mockEnabled
    }

    suspend fun setEnabled(enabled: Boolean) {
        mockEnabled = enabled
    }

    fun authenticate(reason: String, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        authState.value = BiometricAuthState.Authenticating

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            if (mockShouldSucceed) {
                authState.value = BiometricAuthState.Success
                onSuccess.invoke()
            } else {
                authState.value = BiometricAuthState.Failed("Аутентифікацію не розпізнано")
                onFailure.invoke()
            }
        }
    }

    fun setMockShouldSucceed(shouldSucceed: Boolean) {
        mockShouldSucceed = shouldSucceed
    }

    fun setMockSensorAvailable(available: Boolean) {
        mockSensorAvailable = available
    }

    fun getBiometricTypeName(): String = "Відбиток пальця (Mock)"
}