package com.example.planer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.planer.security.BiometricManager

@Composable
fun SecureDeleteDialog(
    biometricManager: BiometricManager,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isAuthenticating by remember { mutableStateOf(false) }
    var authSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (authSuccess) {
        // Після успішної автентифікації - видаляємо
        onConfirm.invoke()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⚠️ Небезпечна операція") },
        text = {
            Column {
                Text("Ви дійсно хочете видалити всі задачі?")
                Spacer(modifier = Modifier.height(16.dp))

                if (isAuthenticating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔄 Сканування...")
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = "❌ $errorMessage",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        isAuthenticating = true
                        errorMessage = null
                        biometricManager.authenticate(
                            reason = "Підтвердіть видалення всіх задач",
                            onSuccess = {
                                isAuthenticating = false
                                authSuccess = true
                            },
                            onFailure = {
                                isAuthenticating = false
                                errorMessage = "Автентифікацію не пройдено"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAuthenticating
                ) {
                    Text("🔐 Підтвердити біометрією")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}