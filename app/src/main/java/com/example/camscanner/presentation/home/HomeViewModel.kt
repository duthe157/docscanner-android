package com.example.camscanner.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.repository.DocumentRepository
import com.example.camscanner.domain.usecase.ManageDocumentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val deleteConfirmDocumentId: String? = null,
    // Merge flow: first select source, then pick target
    val mergeSourceId: String? = null,
    val showMergeTargetPicker: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val manageDocumentUseCase: ManageDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDocuments()
    }

    private fun observeDocuments() {
        viewModelScope.launch {
            repository.getDocuments()
                .catch { e -> _uiState.value = _uiState.value.copy(error = e.message, isLoading = false) }
                .collect { docs ->
                    _uiState.value = _uiState.value.copy(documents = docs, isLoading = false)
                }
        }
    }

    fun requestDeleteDocument(documentId: String) {
        _uiState.value = _uiState.value.copy(deleteConfirmDocumentId = documentId)
    }

    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(deleteConfirmDocumentId = null)
    }

    fun confirmDelete() {
        val id = _uiState.value.deleteConfirmDocumentId ?: return
        _uiState.value = _uiState.value.copy(deleteConfirmDocumentId = null)
        viewModelScope.launch {
            try {
                manageDocumentUseCase.deleteDocument(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ── Merge (Req 6.7) ───────────────────────────────────────────────────────

    fun startMerge(sourceId: String) {
        _uiState.value = _uiState.value.copy(mergeSourceId = sourceId, showMergeTargetPicker = true)
    }

    fun cancelMerge() {
        _uiState.value = _uiState.value.copy(mergeSourceId = null, showMergeTargetPicker = false)
    }

    fun confirmMerge(targetId: String) {
        val sourceId = _uiState.value.mergeSourceId ?: return
        if (sourceId == targetId) {
            _uiState.value = _uiState.value.copy(mergeSourceId = null, showMergeTargetPicker = false)
            return
        }
        _uiState.value = _uiState.value.copy(mergeSourceId = null, showMergeTargetPicker = false)
        viewModelScope.launch {
            try {
                manageDocumentUseCase.mergeDocuments(sourceId, targetId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
