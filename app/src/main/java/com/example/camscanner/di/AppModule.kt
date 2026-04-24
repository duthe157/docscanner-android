package com.example.camscanner.di

import android.content.Context
import com.example.camscanner.data.local.FileStorageManager
import com.example.camscanner.data.local.db.DocumentDao
import com.example.camscanner.data.local.db.PageDao
import com.example.camscanner.data.repository.DocumentRepositoryImpl
import com.example.camscanner.domain.repository.DocumentRepository
import com.example.camscanner.util.BitmapCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFileStorageManager(
        @ApplicationContext context: Context
    ): FileStorageManager = FileStorageManager(context)

    @Provides
    @Singleton
    fun provideBitmapCache(): BitmapCache = BitmapCache()

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        pageDao: PageDao,
        fileStorageManager: FileStorageManager
    ): DocumentRepository = DocumentRepositoryImpl(documentDao, pageDao, fileStorageManager)
}
