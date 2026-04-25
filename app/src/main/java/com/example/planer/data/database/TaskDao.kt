package com.example.planer.data.database

import androidx.room.*
import com.example.planer.Task
import com.example.planer.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE syncStatus = :status")
    suspend fun getTasksBySyncStatus(status: SyncStatus): List<Task>

    @Query("UPDATE tasks SET syncStatus = :status WHERE id = :taskId")
    suspend fun updateSyncStatus(taskId: String, status: SyncStatus)
}