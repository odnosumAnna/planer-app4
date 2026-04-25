package com.example.planer

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String,
    val isActive: Boolean = true,
    val createdAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)