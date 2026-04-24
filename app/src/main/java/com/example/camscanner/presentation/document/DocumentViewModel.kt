package com.example.camscanner.presentation.document

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.Page
import com.example.camscanner.domain.repository.DocumentRepository
import com.example.camscanner.domain.usecase.ManageDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val deleteConfirmPageId: String? = null,
    val showRenameDialog: Boolean = false
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val manageDocumentUseCase: ManageDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            repository.getDocuments().collect { docs ->
                val doc = docs.find { it.id == documentId }
                _uiState.value = _uiState.value.copy(document = doc, isLoading = false)
            }
        }
    }

    fun reorderPages(pageIds: List<String>) {
        val docId = _uiState.value.document?.id ?: return
        viewModelScope.launch {
            try {
                manageDocumentUseCase.reorderPages(docId, pageIds)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun requestDeletePage(pageId: String) {
        _uiState.value = _uiState.value.copy(deleteConfirmPageId = pageId)
    }

    fun cancelDeletePage() {
        _uiState.value = _uiState.value.copy(deleteConfirmPageId = null)
    }

    fun confirmDeletePage() {
        val pageId = _uiState.value.deleteConfirmPageId ?: return
        _uiState.value = _uiState.value.copy(deleteConfirmPageId = null)
        viewModelScope.launch {
            try {
                manageDocumentUseCase.removePage(pageId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun showRenameDialog() {
        _uiState.value = _uiState.value.copy(showRenameDialog = true)
    }

    fun dismissRenameDialog() {
        _uiState.value = _uiState.value.copy(showRenameDialog = false)
    }

    fun renameDocument(newName: String) {
        val doc = _uiState.value.document ?: return
        _uiState.value = _uiState.value.copy(showRenameDialog = false)
        viewModelScope.launch {
            try {
                manageDocumentUseCase.renameDocument(doc, newName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
