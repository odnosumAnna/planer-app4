package com.example.planer.data.database

import androidx.room.TypeConverter
import com.example.planer.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus = SyncStatus.valueOf(status)
}