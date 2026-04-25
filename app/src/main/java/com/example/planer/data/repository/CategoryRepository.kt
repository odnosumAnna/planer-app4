package com.example.planer.data.repository

import android.content.Context
import com.example.planer.Category
import com.example.planer.SyncStatus
import com.example.planer.data.api.MockApiService
import com.example.planer.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val categoryDao = database.categoryDao()
    private val apiService = MockApiService()

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun refreshCategoriesFromServer() {
        withContext(Dispatchers.IO) {
            try {
                val serverCategories = apiService.getCategories()
                serverCategories.forEach { category ->
                    categoryDao.insertCategory(category.copy(syncStatus = SyncStatus.SYNCED))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}