package com.example.planer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.planer.Task
import com.example.planer.data.repository.TaskRepository
import com.example.planer.ui.viewmodel.TaskViewModel
import com.example.planer.ui.viewmodel.TaskViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    taskRepository: TaskRepository
) {
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(taskRepository)
    )

    val scope = rememberCoroutineScope()
    var task by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(taskId) {
        task = taskRepository.getTaskById(taskId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "← Назад",
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            if (task != null) {
                Button(
                    onClick = {
                        scope.launch {
                            taskViewModel.deleteTask(task!!)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Видалити")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        task?.let {
            Text("Назва: ${it.title}", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Опис: ${it.description}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Дедлайн: ${it.deadline}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Категорія: ${it.category}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Статус синхронізації: ${it.syncStatus.name}",
                color = when(it.syncStatus.name) {
                    "PENDING" -> MaterialTheme.colorScheme.primary
                    "ERROR" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}