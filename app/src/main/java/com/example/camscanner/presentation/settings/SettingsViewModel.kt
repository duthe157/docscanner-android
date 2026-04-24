package com.example.camscanner.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.data.local.FileStorageManager
import com.example.camscanner.domain.model.ExportFormat
import com.example.camscanner.domain.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val defaultExportFormat: ExportFormat = ExportFormat.PDF,
    val defaultFilter: FilterType = FilterType.BW,
    val storageUsedBytes: Long = 0L,
    val showDeleteOriginalsConfirm: Boolean = false,
    val isCalculatingStorage: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileStorageManager: FileStorageManager
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("camscanner_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        calculateStorage()
    }

    private fun loadSettings() {
        val format = prefs.getString(KEY_EXPORT_FORMAT, ExportFormat.PDF.name)
            ?.let { runCatching { ExportFormat.valueOf(it) }.getOrDefault(ExportFormat.PDF) }
            ?: ExportFormat.PDF
        val filter = prefs.getString(KEY_DEFAULT_FILTER, FilterType.BW.name)
            ?.let { runCatching { FilterType.valueOf(it) }.getOrDefault(FilterType.BW) }
            ?: FilterType.BW
        _uiState.value = _uiState.value.copy(defaultExportFormat = format, defaultFilter = filter)
    }

    fun setDefaultExportFormat(format: ExportFormat) {
        prefs.edit().putString(KEY_EXPORT_FORMAT, format.name).apply()
        _uiState.value = _uiState.value.copy(defaultExportFormat = format)
    }

    fun setDefaultFilter(filter: FilterType) {
        prefs.edit().putString(KEY_DEFAULT_FILTER, filter.name).apply()
        _uiState.value = _uiState.value.copy(defaultFilter = filter)
    }

    fun calculateStorage() {
        _uiState.value = _uiState.value.copy(isCalculatingStorage = true)
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) { fileStorageManager.calculateTotalSize() }
            _uiState.value = _uiState.value.copy(storageUsedBytes = bytes, isCalculatingStorage = false)
        }
    }

    fun requestDeleteOriginals() {
        _uiState.value = _uiState.value.copy(showDeleteOriginalsConfirm = true)
    }

    fun cancelDeleteOriginals() {
        _uiState.value = _uiState.value.copy(showDeleteOriginalsConfirm = false)
    }

    fun confirmDeleteOriginals() {
        _uiState.value = _uiState.value.copy(showDeleteOriginalsConfirm = false)
        viewModelScope.launch {
            withContext(Dispatchers.IO) { fileStorageManager.deleteOriginals() }
            calculateStorage()
        }
    }

    companion object {
        private const val KEY_EXPORT_FORMAT = "default_export_format"
        private const val KEY_DEFAULT_FILTER = "default_filter"
    }
}
