package com.example.planer.data.repository

import android.content.Context
import com.example.planer.User
import com.example.planer.data.api.MockApiService
import com.example.planer.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val userDao = database.userDao()
    private val apiService = MockApiService()

    fun getUser(): Flow<User?> = userDao.getUser()

    suspend fun refreshUserFromServer() {
        withContext(Dispatchers.IO) {
            try {
                val serverUser = apiService.getUserProfile()
                userDao.insertUser(serverUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}