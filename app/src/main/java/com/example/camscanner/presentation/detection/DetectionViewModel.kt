package com.example.camscanner.presentation.detection

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.domain.usecase.DetectEdgesUseCase
import com.example.camscanner.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class DetectionUiState {
    object Loading : DetectionUiState()
    data class Success(val imageUri: String) : DetectionUiState()
    data class Error(val message: String) : DetectionUiState()
}

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detectEdgesUseCase: DetectEdgesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetectionUiState>(DetectionUiState.Loading)
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    private val _corners = MutableStateFlow<List<PointF>>(emptyList())
    val corners: StateFlow<List<PointF>> = _corners.asStateFlow()

    private var originalCorners: List<PointF> = emptyList()

    fun detectEdges(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = DetectionUiState.Loading
            try {
                // Load + resize on IO thread — never block main thread
                val bitmap = withContext(Dispatchers.IO) {
                    val path = Uri.parse(imageUri).path ?: return@withContext null
                    val opts = BitmapFactory.Options().apply {
                        // Sample down large images before loading into memory
                        inJustDecodeBounds = true
                        BitmapFactory.decodeFile(path, this)
                        inSampleSize = calculateSampleSize(outWidth, outHeight, 1280)
                        inJustDecodeBounds = false
                    }
                    BitmapFactory.decodeFile(path, opts)
                }

                if (bitmap == null) {
                    _uiState.value = DetectionUiState.Error("Không thể đọc ảnh")
                    return@launch
                }

                val result = detectEdgesUseCase.execute(bitmap)
                originalCorners = result.corners
                _corners.value = result.corners
                _uiState.value = DetectionUiState.Success(imageUri)

            } catch (e: Exception) {
                _uiState.value = DetectionUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var size = 1
        while (width / size > maxSize || height / size > maxSize) size *= 2
        return size
    }

    fun updateCorner(index: Int, newPosition: PointF) {
        if (index in _corners.value.indices) {
            _corners.value = _corners.value.toMutableList().apply {
                set(index, newPosition)
            }
        }
    }

    fun resetCorners() {
        _corners.value = originalCorners
    }
}
