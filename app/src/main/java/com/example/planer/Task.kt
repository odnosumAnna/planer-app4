package com.example.planer

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val deadline: String,
    val category: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    PENDING,   // очікує відправки на сервер
    SYNCED,    // синхронізовано
    ERROR      // помилка при синхронізації
}