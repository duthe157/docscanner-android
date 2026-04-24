package com.example.camscanner.domain.usecase

import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.Page
import com.example.camscanner.domain.repository.DocumentRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ManageDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend fun createDocument(name: String? = null): Document {
        val defaultName = name ?: run {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            "Tài liệu ${fmt.format(Date())}"
        }
        return repository.createDocument(defaultName)
    }

    suspend fun renameDocument(document: Document, newName: String) {
        require(newName.isNotBlank()) { "Tên tài liệu không được rỗng" }
        repository.updateDocument(document.copy(name = newName))
    }

    suspend fun deleteDocument(documentId: String) {
        repository.deleteDocument(documentId)
    }

    suspend fun addPage(documentId: String, page: Page) {
        repository.addPage(documentId, page)
    }

    suspend fun removePage(pageId: String) {
        repository.removePage(pageId)
    }

    suspend fun reorderPages(documentId: String, pageIds: List<String>) {
        repository.reorderPages(documentId, pageIds)
    }

    suspend fun mergeDocuments(sourceId: String, targetId: String) {
        repository.mergeDocuments(sourceId, targetId)
    }
}
