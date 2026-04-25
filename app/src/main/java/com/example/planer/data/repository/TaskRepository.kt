package com.example.planer.data.repository

import android.content.Context
import com.example.planer.Task
import com.example.planer.SyncStatus
import com.example.planer.data.api.ApiService
import com.example.planer.data.api.MockApiService
import com.example.planer.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.planer.data.socket.TaskSocketDto

/**
 * Стратегія: Offline-first
 *
 * Застосунок завжди читає з локального сховища (Room).
 * При додаванні/оновленні/видаленні дані спочатку зберігаються локально
 * зі статусом PENDING, а потім фонова синхронізація відправляє їх на сервер.
 *
 * При отриманні даних з сервера (pull-to-refresh) - оновлюємо локальне сховище.
 */
class TaskRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val taskDao = database.taskDao()
    private val apiService: ApiService = MockApiService()

    // Читання - завжди з локальної БД (offline-first)
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(taskId: String): Task? = taskDao.getTaskById(taskId)

    // Запис - спочатку в БД, потім синхронізація
    suspend fun insertTask(task: Task) {
        val taskWithPendingStatus = task.copy(syncStatus = SyncStatus.PENDING)
        taskDao.insertTask(taskWithPendingStatus)
        syncPendingTasks()
    }

    suspend fun updateTask(task: Task) {
        val taskWithPendingStatus = task.copy(syncStatus = SyncStatus.PENDING)
        taskDao.updateTask(taskWithPendingStatus)
        syncPendingTasks()
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        // Видалення також потрібно синхронізувати
        syncPendingTasks()
    }

    // Синхронізація з сервером
    private suspend fun syncPendingTasks() {
        val pendingTasks = taskDao.getTasksBySyncStatus(SyncStatus.PENDING)

        withContext(Dispatchers.IO) {
            pendingTasks.forEach { task ->
                try {
                    // Спроба відправити на сервер
                    if (taskExistsOnServer(task.id)) {
                        apiService.updateTask(task)
                    } else {
                        apiService.createTask(task)
                    }
                    taskDao.updateSyncStatus(task.id, SyncStatus.SYNCED)
                } catch (e: Exception) {
                    taskDao.updateSyncStatus(task.id, SyncStatus.ERROR)
                }
            }
        }
    }

    // Pull-to-refresh: отримуємо дані з сервера та оновлюємо локальну БД
    suspend fun refreshFromServer() {
        withContext(Dispatchers.IO) {
            try {
                val serverTasks = apiService.getTasks()
                serverTasks.forEach { serverTask ->
                    val localTask = taskDao.getTaskById(serverTask.id)
                    if (localTask == null || localTask.syncStatus != SyncStatus.PENDING) {
                        taskDao.insertTask(serverTask.copy(syncStatus = SyncStatus.SYNCED))
                    }
                }
            } catch (e: Exception) {
                // Помилка мережі - ігноруємо, продовжуємо працювати з локальними даними
                e.printStackTrace()
            }
        }
    }

    private suspend fun taskExistsOnServer(taskId: String): Boolean {
        return try {
            apiService.getTaskById(taskId) != null
        } catch (e: Exception) {
            false
        }
    }

    fun handleSocketMessage(json: String) {
        try {
            val dto = Gson().fromJson(json, TaskSocketDto::class.java)

            val task = Task(
                id = dto.id,
                title = dto.title,
                description = dto.description ?: "",
                isCompleted = dto.isCompleted,
                deadline = "2026-01-01",
                category = "WS",
                syncStatus = SyncStatus.SYNCED
            )

            CoroutineScope(Dispatchers.IO).launch {
                val localTask = taskDao.getTaskById(task.id)

                if (localTask == null || localTask.syncStatus != SyncStatus.PENDING) {
                    taskDao.insertTask(task)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}