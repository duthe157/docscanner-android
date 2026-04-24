package com.example.camscanner.di

import android.content.Context
import androidx.room.Room
import com.example.camscanner.data.local.db.AppDatabase
import com.example.camscanner.data.local.db.DocumentDao
import com.example.camscanner.data.local.db.PageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "camscanner_db"
        ).build()
    }

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun providePageDao(database: AppDatabase): PageDao {
        return database.pageDao()
    }
}
