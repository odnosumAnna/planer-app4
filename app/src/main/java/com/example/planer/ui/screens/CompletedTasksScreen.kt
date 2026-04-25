package com.example.planer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.planer.data.repository.TaskRepository
import androidx.compose.ui.unit.dp

@Composable
fun CompletedTasksScreen(taskRepository: TaskRepository) {

    val tasks by taskRepository.getAllTasks().collectAsState(initial = emptyList())

    val completedTasks = tasks.filter { it.isCompleted }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(completedTasks) { task ->
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "✔ ${task.title}", style = MaterialTheme.typography.titleMedium)
                Text(text = task.deadline, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}