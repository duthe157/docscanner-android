package com.example.camscanner.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.camscanner.data.local.entity.DocumentEntity
import com.example.camscanner.data.local.entity.PageEntity

@Database(
    entities = [DocumentEntity::class, PageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun pageDao(): PageDao
}
