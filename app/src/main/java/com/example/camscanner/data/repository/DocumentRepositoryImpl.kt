package com.example.camscanner.data.repository

import android.graphics.PointF
import com.example.camscanner.data.local.FileStorageManager
import com.example.camscanner.data.local.db.DocumentDao
import com.example.camscanner.data.local.db.PageDao
import com.example.camscanner.data.local.entity.DocumentEntity
import com.example.camscanner.data.local.entity.PageEntity
import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.FilterType
import com.example.camscanner.domain.model.Page
import com.example.camscanner.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val pageDao: PageDao,
    private val fileStorageManager: FileStorageManager
) : DocumentRepository {

    override fun getDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities ->
            entities.map { entity ->
                val pages = pageDao.getPagesByDocumentId(entity.id).first()
                entity.toDomain(pages.map { it.toDomain() })
            }
        }
    }

    override suspend fun getDocumentById(id: String): Document? {
        val entity = documentDao.getDocumentById(id) ?: return null
        val pages = pageDao.getPagesByDocumentId(id).first()
        return entity.toDomain(pages.map { it.toDomain() })
    }

    override suspend fun createDocument(name: String): Document {
        val now = System.currentTimeMillis()
        val entity = DocumentEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = now,
            updatedAt = now
        )
        documentDao.insertDocument(entity)
        return entity.toDomain(emptyList())
    }

    override suspend fun updateDocument(document: Document) {
        documentDao.updateDocument(
            DocumentEntity(
                id = document.id,
                name = document.name,
                createdAt = document.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteDocument(documentId: String) {
        val pages = pageDao.getPagesByDocumentId(documentId).first()
        pages.forEach { fileStorageManager.deletePageFiles(it.id) }
        val entity = documentDao.getDocumentById(documentId) ?: return
        documentDao.deleteDocument(entity)
    }

    override suspend fun addPage(documentId: String, page: Page) {
        pageDao.insertPage(page.toEntity())
        touchDocument(documentId)
    }

    override suspend fun removePage(pageId: String) {
        val entity = pageDao.getPageById(pageId) ?: return
        fileStorageManager.deletePageFiles(pageId)
        pageDao.deletePage(entity)
        touchDocument(entity.documentId)
    }

    override suspend fun reorderPages(documentId: String, pageIds: List<String>) {
        pageIds.forEachIndexed { index, pageId ->
            val entity = pageDao.getPageById(pageId) ?: return@forEachIndexed
            pageDao.updatePage(entity.copy(pageOrder = index))
        }
        touchDocument(documentId)
    }

    override suspend fun mergeDocuments(sourceId: String, targetId: String) {
        val sourcePages = pageDao.getPagesByDocumentId(sourceId).first()
        val targetPages = pageDao.getPagesByDocumentId(targetId).first()
        val offset = targetPages.size
        sourcePages.forEachIndexed { index, page ->
            pageDao.updatePage(page.copy(documentId = targetId, pageOrder = offset + index))
        }
        val sourceEntity = documentDao.getDocumentById(sourceId) ?: return
        documentDao.deleteDocument(sourceEntity)
        touchDocument(targetId)
    }

    private suspend fun touchDocument(documentId: String) {
        val entity = documentDao.getDocumentById(documentId) ?: return
        documentDao.updateDocument(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    // --- Mapping ---

    private fun DocumentEntity.toDomain(pages: List<Page>) = Document(
        id = id, name = name, createdAt = createdAt, updatedAt = updatedAt, pages = pages
    )

    private fun PageEntity.toDomain() = Page(
        id = id,
        documentId = documentId,
        order = pageOrder,
        originalPath = originalPath,
        processedPath = processedPath,
        previewPath = previewPath,
        filter = FilterType.valueOf(filter),
        brightness = brightness,
        contrast = contrast,
        rotation = rotation,
        corners = cornersJson.parseCorners()
    )

    private fun Page.toEntity() = PageEntity(
        id = id,
        documentId = documentId,
        pageOrder = order,
        originalPath = originalPath,
        processedPath = processedPath,
        previewPath = previewPath,
        filter = filter.name,
        brightness = brightness,
        contrast = contrast,
        rotation = rotation,
        cornersJson = corners.toJson()
    )

    private fun String.parseCorners(): List<PointF> {
        return try {
            val arr = JSONArray(this)
            (0 until arr.length()).map { i ->
                val pt = arr.getJSONArray(i)
                PointF(pt.getDouble(0).toFloat(), pt.getDouble(1).toFloat())
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun List<PointF>.toJson(): String {
        val arr = JSONArray()
        forEach { pt ->
            arr.put(JSONArray().apply { put(pt.x.toDouble()); put(pt.y.toDouble()) })
        }
        return arr.toString()
    }
}
