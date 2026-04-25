package com.example.planer.data.api

import com.example.planer.Task
import com.example.planer.Category
import com.example.planer.User
import kotlinx.coroutines.delay

/**
 * API Контракт:
 *
 * REST API Endpoints:
 *
 * 1. GET    /api/tasks          - отримати всі задачі
 *    Returns: List<Task>
 *
 * 2. GET    /api/tasks/{id}     - отримати задачу за ID
 *    Returns: Task
 *
 * 3. POST   /api/tasks          - створити нову задачу
 *    Body: { title, description, deadline, category }
 *    Returns: Task (з присвоєним ID)
 *
 * 4. PUT    /api/tasks/{id}     - оновити задачу
 *    Body: { title, description, isCompleted, deadline, category }
 *    Returns: Task
 *
 * 5. DELETE /api/tasks/{id}     - видалити задачу
 *    Returns: { success: boolean }
 *
 * 6. GET    /api/categories     - отримати всі категорії
 *    Returns: List<Category>
 *
 * 7. GET    /api/user/profile   - отримати профіль користувача
 *    Returns: User
 */
interface ApiService {
    suspend fun getTasks(): List<Task>
    suspend fun getTaskById(taskId: String): Task?
    suspend fun createTask(task: Task): Task
    suspend fun updateTask(task: Task): Task
    suspend fun deleteTask(taskId: String): Boolean
    suspend fun getCategories(): List<Category>
    suspend fun getUserProfile(): User
}

// Mock реалізація для тестування (без реального сервера)
class MockApiService : ApiService {
    private var tasks = mutableListOf<Task>()
    private var categories = mutableListOf<Category>()
    private var user: User? = null

    init {
        // Ініціалізація тестовими даними
        tasks.addAll(listOf(
            Task(
                id = "1",
                title = "Лаба",
                description = "Здати завтра",
                deadline = "2026-04-10",
                category = "Навчання"
            ),
            Task(
                id = "2",
                title = "Зал",
                description = "Тренування",
                deadline = "2026-04-08",
                category = "Спорт"
            )
        ))

        categories.addAll(listOf(
            Category(
                id = "cat1",
                name = "Навчання",
                color = "#4CAF50",
                createdAt = "2026-04-01"
            ),
            Category(
                id = "cat2",
                name = "Спорт",
                color = "#2196F3",
                createdAt = "2026-04-01"
            ),
            Category(
                id = "cat3",
                name = "Робота",
                color = "#FF9800",
                createdAt = "2026-04-01"
            )
        ))

        user = User(
            id = "current_user",
            name = "Анна",
            email = "anna@example.com",
            createdAt = "2026-04-01"
        )
    }

    override suspend fun getTasks(): List<Task> {
        delay(500) // Симуляція мережевої затримки
        return tasks.toList()
    }

    override suspend fun getTaskById(taskId: String): Task? {
        delay(300)
        return tasks.find { it.id == taskId }
    }

    override suspend fun createTask(task: Task): Task {
        delay(500)
        val newTask = task.copy(id = (tasks.size + 1).toString())
        tasks.add(newTask)
        return newTask
    }

    override suspend fun updateTask(task: Task): Task {
        delay(500)
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            tasks[index] = task
        }
        return task
    }

    override suspend fun deleteTask(taskId: String): Boolean {
        delay(500)
        return tasks.removeIf { it.id == taskId }
    }

    override suspend fun getCategories(): List<Category> {
        delay(300)
        return categories.toList()
    }

    override suspend fun getUserProfile(): User {
        delay(300)
        return user ?: User(
            id = "current_user",
            name = "Гість",
            email = "guest@example.com",
            createdAt = "2026-04-01"
        )
    }
}