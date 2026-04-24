package com.example.camscanner.presentation.edit

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.data.local.FileStorageManager
import com.example.camscanner.domain.model.FilterType
import com.example.camscanner.domain.model.Page
import com.example.camscanner.domain.repository.DocumentRepository
import com.example.camscanner.domain.usecase.ManageDocumentUseCase
import com.example.camscanner.domain.usecase.ProcessImageUseCase
import com.example.camscanner.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

data class EditUiState(
    val previewBitmap: Bitmap? = null,
    val filter: FilterType = FilterType.BW,
    val brightness: Float = 0f,
    val contrast: Float = 10f,
    val rotation: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedDocumentId: String? = null,
    val error: String? = null
)

@HiltViewModel
class EditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val processImageUseCase: ProcessImageUseCase,
    private val fileStorageManager: FileStorageManager,
    private val manageDocumentUseCase: ManageDocumentUseCase,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("camscanner_settings", Context.MODE_PRIVATE)

    private fun defaultFilter(): FilterType =
        prefs.getString("default_filter", FilterType.BW.name)
            ?.let { runCatching { FilterType.valueOf(it) }.getOrDefault(FilterType.BW) }
            ?: FilterType.BW

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private var sourceBitmap: Bitmap? = null
    private var sourceCorners: List<PointF> = emptyList()
    private var editingPageId: String? = null   // non-null = editing existing page

    /** Called when coming from Detection screen (new scan) */
    fun loadBitmap(bitmap: Bitmap, corners: List<PointF>) {
        sourceBitmap = bitmap
        sourceCorners = corners
        editingPageId = null
        // Apply default filter from Settings (Req 11.2)
        _uiState.value = _uiState.value.copy(filter = defaultFilter())
        viewModelScope.launch { renderPreview() }
    }

    /** Called when editing an existing saved page */
    fun loadExistingPage(pageId: String) {
        if (editingPageId == pageId) return   // already loaded
        editingPageId = pageId
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val page = findPage(pageId)
                if (page == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Không tìm thấy trang")
                    return@launch
                }
                val bitmap = withContext(Dispatchers.IO) {
                    // Load original if exists, else processed
                    val origFile = java.io.File(page.originalPath)
                    val path = if (origFile.exists()) page.originalPath else page.processedPath
                    BitmapFactory.decodeFile(path)
                }
                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Không thể đọc ảnh")
                    return@launch
                }
                sourceBitmap = bitmap
                sourceCorners = page.corners
                _uiState.value = _uiState.value.copy(
                    filter = page.filter,
                    brightness = page.brightness.toFloat(),
                    contrast = page.contrast.toFloat(),
                    rotation = page.rotation,
                    isLoading = false
                )
                renderPreview()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private suspend fun findPage(pageId: String): Page? {
        val docs = withContext(Dispatchers.IO) {
            documentRepository.getDocuments().first()
        }
        for (doc in docs) {
            for (page in doc.pages) {
                if (page.id == pageId) return page
            }
        }
        return null
    }

    fun applyFilter(filter: FilterType) {
        _uiState.value = _uiState.value.copy(filter = filter)
        viewModelScope.launch { renderPreview() }
    }

    fun setBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value)
        viewModelScope.launch { renderPreview() }
    }

    fun setContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value)
        viewModelScope.launch { renderPreview() }
    }

    fun rotate() {
        _uiState.value = _uiState.value.copy(rotation = (_uiState.value.rotation + 90) % 360)
        viewModelScope.launch { renderPreview() }
    }

    fun saveToDocument(documentId: String?) {
        val src = sourceBitmap ?: return
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val pageId = editingPageId ?: UUID.randomUUID().toString()

                val fullBitmap = withContext(Dispatchers.Default) {
                    processImageUseCase.execute(
                        bitmap = src,
                        corners = sourceCorners,
                        filter = state.filter,
                        brightness = state.brightness.toInt(),
                        contrast = state.contrast.toInt(),
                        rotation = state.rotation,
                        isPreview = false
                    )
                }

                val originalPath: String
                val processedPath: String
                val previewPath: String
                withContext(Dispatchers.IO) {
                    originalPath = fileStorageManager.saveOriginal(pageId, src)
                    processedPath = fileStorageManager.saveProcessed(pageId, fullBitmap)
                    val thumb = ImageUtils.createPreviewBitmap(fullBitmap, 200)
                    previewPath = fileStorageManager.savePreview(pageId, thumb)
                    thumb.recycle()
                    fullBitmap.recycle()
                }

                val docId = documentId ?: manageDocumentUseCase.createDocument().id
                val existingDoc = documentRepository.getDocumentById(docId)

                // If editing existing page, remove old then re-add; else append
                val pageOrder = if (editingPageId != null) {
                    existingDoc?.pages?.find { it.id == pageId }?.order ?: 0
                } else {
                    existingDoc?.pages?.size ?: 0
                }

                if (editingPageId != null) {
                    manageDocumentUseCase.removePage(pageId)
                }

                val page = Page(
                    id = pageId,
                    documentId = docId,
                    order = pageOrder,
                    originalPath = originalPath,
                    processedPath = processedPath,
                    previewPath = previewPath,
                    filter = state.filter,
                    brightness = state.brightness.toInt(),
                    contrast = state.contrast.toInt(),
                    rotation = state.rotation,
                    corners = sourceCorners
                )
                manageDocumentUseCase.addPage(docId, page)

                // Req 10.6: recycle source bitmap ngay sau khi lưu xong
                sourceBitmap?.recycle()
                sourceBitmap = null

                _uiState.value = _uiState.value.copy(isSaving = false, savedDocumentId = docId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    suspend fun getProcessedBitmap(): Bitmap? {
        val src = sourceBitmap ?: return null
        return withContext(Dispatchers.Default) {
            processImageUseCase.execute(
                bitmap = src,
                corners = sourceCorners,
                filter = _uiState.value.filter,
                brightness = _uiState.value.brightness.toInt(),
                contrast = _uiState.value.contrast.toInt(),
                rotation = _uiState.value.rotation,
                isPreview = false
            )
        }
    }

    private suspend fun renderPreview() {
        val src = sourceBitmap ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val preview = withContext(Dispatchers.Default) {
                processImageUseCase.execute(
                    bitmap = src,
                    corners = sourceCorners,
                    filter = _uiState.value.filter,
                    brightness = _uiState.value.brightness.toInt(),
                    contrast = _uiState.value.contrast.toInt(),
                    rotation = _uiState.value.rotation,
                    isPreview = true
                )
            }
            _uiState.value = _uiState.value.copy(previewBitmap = preview, isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sourceBitmap?.recycle()
    }
}
