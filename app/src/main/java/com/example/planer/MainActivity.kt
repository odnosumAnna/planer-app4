package com.example.planer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.fragment.app.FragmentActivity
import com.example.planer.data.repository.TaskRepository
import com.example.planer.data.repository.CategoryRepository
import com.example.planer.data.repository.UserRepository
import com.example.planer.ui.screens.*
import com.example.planer.ui.theme.PlanerTheme
import com.example.planer.data.socket.SocketManager
import com.example.planer.data.socket.FakeSocketManager
import com.example.planer.security.BiometricManager

class MainActivity : FragmentActivity() {

    private lateinit var taskRepository: TaskRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var userRepository: UserRepository
    private lateinit var biometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ініціалізація репозиторіїв
        taskRepository = TaskRepository(this)
        categoryRepository = CategoryRepository(this)
        userRepository = UserRepository(this)
        biometricManager = BiometricManager(this)

        setContent {
            PlanerTheme {
                PlanerApp(
                    taskRepository = taskRepository,
                    categoryRepository = categoryRepository,
                    userRepository = userRepository,
                    biometricManager = biometricManager
                )
            }
        }
    }
}

@Composable
fun PlanerApp(
    taskRepository: TaskRepository,
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    biometricManager: BiometricManager
) {
    val navController = rememberNavController()
    val socketManager = remember { SocketManager() }
    val fakeSocket = remember { FakeSocketManager() }

    var isUnlocked by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        taskRepository.refreshFromServer()

        fakeSocket.onMessage { message ->
            taskRepository.handleSocketMessage(message)
        }

        fakeSocket.start()
    }

    // Початкове завантаження даних з сервера
    LaunchedEffect(Unit) {
        taskRepository.refreshFromServer()
        categoryRepository.refreshCategoriesFromServer()
        userRepository.refreshUserFromServer()

        socketManager.connect("wss://echo.websocket.events")

        socketManager.onMessage { message ->
            taskRepository.handleSocketMessage(message)
        }

        kotlinx.coroutines.delay(2000)

        socketManager.send("""{
        "id": "ws_1",
        "title": "Task from WebSocket",
        "description": "Real-time update",
        "isCompleted": false
    }""")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("list") },
                    icon = { Text("📋") },
                    label = { Text("Задачі") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("add") },
                    icon = { Text("➕") },
                    label = { Text("Додати") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Text("👤") },
                    label = { Text("Профіль") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("completed") },
                    icon = { Text("✔") },
                    label = { Text("Виконані") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("stats") },
                    icon = { Text("📊") },
                    label = { Text("Статистика") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("security") },
                    icon = { Text("🔐") },
                    label = { Text("Безпека") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("list") {
                TaskListScreen(
                    navController = navController,
                    taskRepository = taskRepository
                )
            }

            composable("detail/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                TaskDetailScreen(
                    taskId = taskId,
                    navController = navController,
                    taskRepository = taskRepository
                )
            }

            composable("add") {
                AddTaskScreen(
                    navController = navController,
                    taskRepository = taskRepository
                )
            }

            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    userRepository = userRepository
                )
            }

            composable("completed") {
                CompletedTasksScreen(taskRepository)
            }

            composable("stats") {
                StatisticsScreen(taskRepository)
            }

            composable("security") {
                SecuritySettingsScreen(
                    navController = navController,
                    biometricManager = biometricManager
                )
            }

            composable("lock") {
                BiometricLockScreen(
                    navController = navController,
                    biometricManager = biometricManager,
                    onUnlock = {
                        isUnlocked = true
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            socketManager.disconnect()
        }
    }
}