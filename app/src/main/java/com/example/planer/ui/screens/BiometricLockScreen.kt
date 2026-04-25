package com.example.planer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.planer.security.BiometricManager
import com.example.planer.security.BiometricAuthState
import com.example.planer.ui.viewmodel.BiometricSettingsViewModel
import com.example.planer.ui.viewmodel.BiometricSettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricLockScreen(
    navController: NavController,
    biometricManager: BiometricManager,
    onUnlock: () -> Unit
) {
    val viewModel: BiometricSettingsViewModel = viewModel(
        factory = BiometricSettingsViewModelFactory(biometricManager)
    )

    val isEnabled by viewModel.isBiometricEnabled.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val biometricTypeName by viewModel.biometricTypeName.collectAsState()

    var autoStarted by remember { mutableStateOf(false) }

    // Автоматичний запуск біометрії при відкритті екрану
    LaunchedEffect(Unit) {
        if (isEnabled && !autoStarted) {
            autoStarted = true
            viewModel.testAuthentication(
                onSuccess = { onUnlock.invoke() },
                onFailure = { }
            )
        }
    }

    // Слідкуємо за зміною стану
    LaunchedEffect(authState) {
        when (authState) {
            is BiometricAuthState.Success -> {
                onUnlock.invoke()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Блокування") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Іконка
                Text(
                    text = when (authState) {
                        is BiometricAuthState.Authenticating -> "🔄"
                        is BiometricAuthState.Success -> "✅"
                        is BiometricAuthState.Failed -> "❌"
                        is BiometricAuthState.Unavailable -> "⚠️"
                        else -> "🔒"
                    },
                    style = MaterialTheme.typography.displayLarge
                )

                // Статус
                Text(
                    text = when (authState) {
                        is BiometricAuthState.Authenticating -> "Сканування..."
                        is BiometricAuthState.Success -> "Успішно!"
                        is BiometricAuthState.Failed -> (authState as BiometricAuthState.Failed).message
                        is BiometricAuthState.Unavailable -> (authState as BiometricAuthState.Unavailable).reason
                        else -> "Підтвердіть особу"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                // Кнопка повторної спроби
                if (authState is BiometricAuthState.Failed || authState is BiometricAuthState.Unavailable) {
                    Button(
                        onClick = {
                            viewModel.testAuthentication(
                                onSuccess = { onUnlock.invoke() },
                                onFailure = { }
                            )
                        }
                    ) {
                        Text("Спробувати ще раз")
                    }
                }

                // Кнопка виходу
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Вихід")
                }
            }
        }
    }
}