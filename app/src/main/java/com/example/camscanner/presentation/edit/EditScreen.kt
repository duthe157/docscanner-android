package com.example.camscanner.presentation.edit

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.camscanner.domain.model.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    bitmap: Bitmap? = null,
    corners: List<PointF> = emptyList(),
    documentId: String? = null,
    existingPageId: String? = null,
    onSaved: (documentId: String) -> Unit = {},
    onRetake: () -> Unit = {},
    viewModel: EditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load source
    LaunchedEffect(bitmap, existingPageId) {
        when {
            existingPageId != null -> viewModel.loadExistingPage(existingPageId)
            bitmap != null -> viewModel.loadBitmap(bitmap, corners)
        }
    }

    // Navigate when save completes
    LaunchedEffect(uiState.savedDocumentId) {
        uiState.savedDocumentId?.let { onSaved(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingPageId != null) "Chỉnh sửa trang" else "Chỉnh sửa") },
                navigationIcon = {
                    IconButton(onClick = onRetake) {
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Preview area ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                val displayBitmap = uiState.previewBitmap ?: bitmap
                if (displayBitmap != null) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
                if (uiState.isLoading || uiState.isSaving) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (uiState.isSaving) "Đang lưu..." else "Đang xử lý...",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ── Controls ──────────────────────────────────────────────────────
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {

                    // Filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            FilterType.ORIGINAL to "Gốc",
                            FilterType.BW to "B&W",
                            FilterType.COLOR to "Màu",
                            FilterType.DARK to "Tối"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = uiState.filter == type,
                                onClick = { viewModel.applyFilter(type) },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isSaving
                            )
                        }
                    }

                    // Brightness slider
                    SliderRow(
                        label = "Sáng",
                        value = uiState.brightness,
                        onValueChange = { viewModel.setBrightness(it) },
                        valueRange = -100f..100f,
                        enabled = !uiState.isSaving
                    )

                    // Contrast slider
                    SliderRow(
                        label = "Tương phản",
                        value = uiState.contrast,
                        onValueChange = { viewModel.setContrast(it) },
                        valueRange = -100f..100f,
                        enabled = !uiState.isSaving
                    )

                    // Action row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate button
                        OutlinedButton(
                            onClick = { viewModel.rotate() },
                            enabled = !uiState.isSaving,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.RotateRight, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Xoay 90°")
                        }

                        // Save button
                        Button(
                            onClick = { viewModel.saveToDocument(documentId) },
                            enabled = !uiState.isLoading && !uiState.isSaving && (uiState.previewBitmap != null || bitmap != null),
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Lưu trang", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Error
                    uiState.error?.let { err ->
                        Text(
                            err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(72.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${value.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(32.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
