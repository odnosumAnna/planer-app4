package com.example.planer.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.Executor

private val FragmentActivity.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

// Стани автентифікації
sealed class BiometricAuthState {
    object Idle : BiometricAuthState()
    object Authenticating : BiometricAuthState()
    object Success : BiometricAuthState()
    data class Failed(val message: String) : BiometricAuthState()
    data class Unavailable(val reason: String) : BiometricAuthState()
}

// Типи біометрії на пристрої
enum class BiometricType {
    FINGERPRINT,
    FACE_RECOGNITION,
    IRIS,
    NONE
}

class BiometricManager(private val activity: FragmentActivity) {  // Зміна: Context -> FragmentActivity

    private val biometricManager = BiometricManager.from(activity)
    private val authState = MutableStateFlow<BiometricAuthState>(BiometricAuthState.Idle)
    val authenticationState: StateFlow<BiometricAuthState> = authState

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    private var onSuccessCallback: (() -> Unit)? = null
    private var onFailureCallback: (() -> Unit)? = null

    init {
        setupBiometricPrompt()
    }

    private fun setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        authState.value = BiometricAuthState.Failed("Скасовано користувачем")
                        onFailureCallback?.invoke()
                    }
                    else -> {
                        authState.value = BiometricAuthState.Failed(errString.toString())
                        onFailureCallback?.invoke()
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authState.value = BiometricAuthState.Success
                onSuccessCallback?.invoke()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authState.value = BiometricAuthState.Failed("Аутентифікацію не розпізнано")
                onFailureCallback?.invoke()
            }
        }

        biometricPrompt = BiometricPrompt(activity, executor, callback)
    }

    // Перевірка доступності біометрії
    fun checkAvailability(): BiometricType {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                when {
                    hasFingerprint() -> BiometricType.FINGERPRINT
                    hasFaceRecognition() -> BiometricType.FACE_RECOGNITION
                    hasIris() -> BiometricType.IRIS
                    else -> BiometricType.FINGERPRINT
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                authState.value = BiometricAuthState.Unavailable("На пристрої немає датчика")
                BiometricType.NONE
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                authState.value = BiometricAuthState.Unavailable("Датчик тимчасово недоступний")
                BiometricType.NONE
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                authState.value = BiometricAuthState.Unavailable("Біометрію не налаштовано")
                BiometricType.NONE
            }
            else -> {
                authState.value = BiometricAuthState.Unavailable("Біометрія недоступна")
                BiometricType.NONE
            }
        }
    }

    // Перевірка чи увімкнена біометрія в налаштуваннях додатку
    fun isEnabledByUser(): Flow<Boolean> {
        val key = booleanPreferencesKey("biometric_enabled")
        return activity.dataStore.data.map { preferences ->
            preferences[key] ?: false
        }
    }

    // Зберегти налаштування біометрії
    suspend fun setEnabled(enabled: Boolean) {
        val key = booleanPreferencesKey("biometric_enabled")
        activity.dataStore.edit { preferences ->
            preferences[key] = enabled
        }
    }

    // Запит автентифікації
    fun authenticate(reason: String, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        onSuccessCallback = onSuccess
        onFailureCallback = onFailure

        val type = checkAvailability()
        if (type == BiometricType.NONE) {
            authState.value = BiometricAuthState.Unavailable("Біометрія не підтримується")
            onFailure.invoke()
            return
        }

        val title = when (type) {
            BiometricType.FINGERPRINT -> "Вхід за відбитком пальця"
            BiometricType.FACE_RECOGNITION -> "Вхід за Face ID"
            BiometricType.IRIS -> "Вхід за Iris"
            BiometricType.NONE -> "Вхід"
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Підтвердіть особу")
            .setDescription(reason)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        authState.value = BiometricAuthState.Authenticating

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            authState.value = BiometricAuthState.Failed("Помилка: ${e.message}")
            onFailure.invoke()
        }
    }

    // Отримати текстовий опис типу біометрії
    fun getBiometricTypeName(): String {
        return when (checkAvailability()) {
            BiometricType.FINGERPRINT -> "Відбиток пальця"
            BiometricType.FACE_RECOGNITION -> "Face ID"
            BiometricType.IRIS -> "Iris"
            BiometricType.NONE -> "Недоступно"
        }
    }

    private fun hasFingerprint(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FINGERPRINT)
        } else false
    }

    private fun hasFaceRecognition(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_FACE)
        } else false
    }

    private fun hasIris(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_IRIS)
        } else false
    }
}