package com.example.planer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.planer.data.repository.TaskRepository

@Composable
fun StatisticsScreen(taskRepository: TaskRepository) {

    val tasks by taskRepository.getAllTasks().collectAsState(initial = emptyList())

    val total = tasks.size
    val completed = tasks.count { it.isCompleted }
    val active = total - completed

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("Всього задач: $total")
            Text("Виконано: $completed")
            Text("Активні: $active")
        }
    }
}