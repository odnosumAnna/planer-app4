package com.example.planer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "current_user",
    val name: String,
    val email: String,
    val isPremium: Boolean = false,
    val createdAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)