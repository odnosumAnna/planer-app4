package com.example.planer.data.socket

data class TaskSocketDto(
    val id: String,
    val title: String,
    val description: String?,
    val isCompleted: Boolean
)