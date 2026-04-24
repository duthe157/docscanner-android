package com.example.camscanner.presentation.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.data.local.FileStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class CameraUiState {
    object Idle : CameraUiState()
    object Capturing : CameraUiState()
    data class Captured(val imageUri: Uri) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val fileStorageManager: FileStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled.asStateFlow()

    fun toggleFlash() {
        _flashEnabled.value = !_flashEnabled.value
    }

    fun onPhotoCaptured(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Captured(uri)
        }
    }

    fun onCaptureError(exception: Exception) {
        _uiState.value = CameraUiState.Error(exception.message ?: "Unknown error")
    }

    fun getTempImageFile(): File {
        return File(fileStorageManager.getTempDir(), "temp_${System.currentTimeMillis()}.jpg")
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
    }
}
