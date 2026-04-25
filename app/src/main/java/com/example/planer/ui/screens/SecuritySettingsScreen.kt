package com.example.planer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.planer.security.BiometricManager
import com.example.planer.security.BiometricAuthState
import com.example.planer.security.BiometricType
import com.example.planer.ui.viewmodel.BiometricSettingsViewModel
import com.example.planer.ui.viewmodel.BiometricSettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    biometricManager: BiometricManager
) {
    val viewModel: BiometricSettingsViewModel = viewModel(
        factory = BiometricSettingsViewModelFactory(biometricManager)
    )

    val isEnabled by viewModel.isBiometricEnabled.collectAsState()
    val biometricType by viewModel.biometricType.collectAsState()
    val biometricTypeName by viewModel.biometricTypeName.collectAsState()
    val authState by viewModel.authState.collectAsState()

    var showTestDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Налаштування безпеки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Статус біометрії
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔐 Біометрична автентифікація",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${viewModel.getBiometricIcon()} Тип датчика:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = biometricTypeName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (biometricType != BiometricType.NONE)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (biometricType) {
                                BiometricType.NONE -> "Біометрія недоступна на цьому пристрої"
                                else -> "Біометрія доступна"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (biometricType != BiometricType.NONE)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Тумблер увімкнення біометрії
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Увімкнути біометричний вхід",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Використовуйте ${biometricTypeName.toLowerCase()} для входу",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.toggleBiometric(it) },
                            enabled = biometricType != BiometricType.NONE
                        )
                    }
                }
            }

            // Кнопка тестування
            if (isEnabled && biometricType != BiometricType.NONE) {
                item {
                    Button(
                        onClick = { viewModel.testAuthentication(
                            onSuccess = { showTestDialog = true },
                            onFailure = { }
                        ) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("🧪 Тестувати біометрію")
                    }
                }
            }

            // Статус автентифікації
            when (authState) {
                is BiometricAuthState.Authenticating -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🔄 Сканування...", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                is BiometricAuthState.Success -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("✅ Успішна автентифікація!", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                is BiometricAuthState.Failed -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("❌ Помилка", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = (authState as BiometricAuthState.Failed).message,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                is BiometricAuthState.Unavailable -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("⚠️ Біометрія недоступна", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = (authState as BiometricAuthState.Unavailable).reason,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // Діалог для тестування
    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = { Text("✅ Успішно!") },
            text = { Text("Біометрична автентифікація пройдена успішно") },
            confirmButton = {
                TextButton(onClick = { showTestDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}