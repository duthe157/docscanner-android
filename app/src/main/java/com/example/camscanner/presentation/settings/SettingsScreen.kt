package com.example.camscanner.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.camscanner.domain.model.ExportFormat
import com.example.camscanner.domain.model.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showDeleteOriginalsConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteOriginals() },
            title = { Text("Xóa ảnh gốc?") },
            text = { Text("Ảnh gốc sẽ bị xóa để giải phóng dung lượng. Ảnh đã xử lý vẫn được giữ lại.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteOriginals() }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteOriginals() }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Export format
            SettingsSection(title = "Định dạng xuất mặc định") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = uiState.defaultExportFormat == fmt,
                            onClick = { viewModel.setDefaultExportFormat(fmt) },
                            label = { Text(fmt.name) }
                        )
                    }
                }
            }

            Divider()

            // Default filter
            SettingsSection(title = "Bộ lọc mặc định khi scan") {
                val filterLabels = mapOf(
                    FilterType.ORIGINAL to "Gốc",
                    FilterType.BW to "Đen trắng",
                    FilterType.COLOR to "Màu sắc",
                    FilterType.DARK to "Tối"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterType.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { filter ->
                                FilterChip(
                                    selected = uiState.defaultFilter == filter,
                                    onClick = { viewModel.setDefaultFilter(filter) },
                                    label = { Text(filterLabels[filter] ?: filter.name) }
                                )
                            }
                        }
                    }
                }
            }

            Divider()

            // Storage info
            SettingsSection(title = "Dung lượng đang dùng") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isCalculatingStorage) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            formatBytes(uiState.storageUsedBytes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(onClick = { viewModel.calculateStorage() }) {
                        Text("Làm mới")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.requestDeleteOriginals() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xóa ảnh gốc (giữ ảnh đã xử lý)")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024 * 1024))} GB"
    }
}
