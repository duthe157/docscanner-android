package com.example.camscanner.data.local.db

import androidx.room.*
import com.example.camscanner.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity)

    @Update
    suspend fun updatePage(page: PageEntity)

    @Delete
    suspend fun deletePage(page: PageEntity)

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageOrder ASC")
    fun getPagesByDocumentId(documentId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :pageId LIMIT 1")
    suspend fun getPageById(pageId: String): PageEntity?

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesByDocumentId(documentId: String)
}
