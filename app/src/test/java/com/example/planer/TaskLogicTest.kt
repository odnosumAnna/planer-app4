package com.example.planer

import org.junit.Assert.*
import org.junit.Test

class TaskLogicTest {

    // ---------- TEST DATA ----------
    private fun sampleTasks() = listOf(
        Task("1", "Task 1", "desc", true, "2026-01-01", "A", SyncStatus.SYNCED),
        Task("2", "Task 2", "desc", false, "2026-01-01", "A", SyncStatus.PENDING),
        Task("3", "Task 3", "desc", true, "2026-01-01", "B", SyncStatus.ERROR)
    )

    // ---------- COMPLETED TESTS ----------

    @Test
    fun completedTasks_onlyCompletedReturned() {
        val result = sampleTasks().filter { it.isCompleted }
        assertEquals(2, result.size)
    }

    @Test
    fun completedTasks_noneCompleted() {
        val tasks = listOf(
            Task("1", "T", "", false, "2026", "A", SyncStatus.SYNCED)
        )
        val result = tasks.filter { it.isCompleted }
        assertTrue(result.isEmpty())
    }

    @Test
    fun completedTasks_allCompleted() {
        val tasks = listOf(
            Task("1", "T", "", true, "2026", "A", SyncStatus.SYNCED)
        )
        val result = tasks.filter { it.isCompleted }
        assertEquals(1, result.size)
    }

    // ---------- STATISTICS ----------

    @Test
    fun stats_totalCount() {
        val tasks = sampleTasks()
        assertEquals(3, tasks.size)
    }

    @Test
    fun stats_completedCount() {
        val tasks = sampleTasks()
        val completed = tasks.count { it.isCompleted }
        assertEquals(2, completed)
    }

    @Test
    fun stats_activeCount() {
        val tasks = sampleTasks()
        val active = tasks.count { !it.isCompleted }
        assertEquals(1, active)
    }

    // ---------- SYNC STATUS ----------

    @Test
    fun syncStatus_pendingExists() {
        val tasks = sampleTasks()
        val pending = tasks.any { it.syncStatus == SyncStatus.PENDING }
        assertTrue(pending)
    }

    @Test
    fun syncStatus_errorExists() {
        val tasks = sampleTasks()
        val error = tasks.any { it.syncStatus == SyncStatus.ERROR }
        assertTrue(error)
    }

    @Test
    fun syncStatus_syncedExists() {
        val tasks = sampleTasks()
        val synced = tasks.any { it.syncStatus == SyncStatus.SYNCED }
        assertTrue(synced)
    }

    // ---------- CATEGORY ----------

    @Test
    fun category_filterWorks() {
        val tasks = sampleTasks()
        val result = tasks.filter { it.category == "A" }
        assertEquals(2, result.size)
    }

    // ---------- DEADLINE ----------

    @Test
    fun deadline_notEmpty() {
        val task = sampleTasks().first()
        assertTrue(task.deadline.isNotEmpty())
    }

    // ---------- TASK CREATION ----------

    @Test
    fun task_creationDefaults() {
        val task = Task(
            title = "Test",
            description = "",
            deadline = "2026",
            category = "A"
        )
        assertEquals(false, task.isCompleted)
    }

    // ---------- UPDATE LOGIC ----------

    @Test
    fun task_markCompleted() {
        val task = sampleTasks().first()
        val updated = task.copy(isCompleted = true)
        assertTrue(updated.isCompleted)
    }

    // ---------- COUNT ----------

    @Test
    fun list_notEmpty() {
        val tasks = sampleTasks()
        assertTrue(tasks.isNotEmpty())
    }

    @Test
    fun list_sizeCorrect() {
        val tasks = sampleTasks()
        assertEquals(3, tasks.size)
    }
}