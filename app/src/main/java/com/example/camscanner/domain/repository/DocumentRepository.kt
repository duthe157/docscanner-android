package com.example.camscanner.domain.repository

import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocuments(): Flow<List<Document>>
    suspend fun getDocumentById(id: String): Document?
    suspend fun createDocument(name: String): Document
    suspend fun updateDocument(document: Document)
    suspend fun deleteDocument(documentId: String)
    suspend fun addPage(documentId: String, page: Page)
    suspend fun removePage(pageId: String)
    suspend fun reorderPages(documentId: String, pageIds: List<String>)
    suspend fun mergeDocuments(sourceId: String, targetId: String)
}
