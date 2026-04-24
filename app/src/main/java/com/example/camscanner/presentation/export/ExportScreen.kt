package com.example.camscanner.presentation.export

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.example.camscanner.domain.model.ExportFormat
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    documentId: String,
    fallbackBitmap: Bitmap? = null,
    onDone: () -> Unit = {},
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Xuất tài liệu", style = MaterialTheme.typography.titleMedium)
                        uiState.document?.let {
                            Text(
                                it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Page previews
                    val pages = uiState.document?.pages?.sortedBy { it.order }
                    if (!pages.isNullOrEmpty()) {
                        Text(
                            "${pages.size} trang",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pages) { page ->
                                val f = File(page.previewPath)
                                if (f.exists()) {
                                    AsyncImage(
                                        model = f,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp, 110.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    } else if (fallbackBitmap != null) {
                        // Single bitmap preview (legacy path)
                        androidx.compose.foundation.Image(
                            bitmap = fallbackBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))

                    // Format selector
                    Text(
                        "Định dạng xuất",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExportFormat.entries.forEach { fmt ->
                            FormatCard(
                                format = fmt,
                                selected = uiState.selectedFormat == fmt,
                                onClick = { viewModel.selectFormat(fmt) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Export actions
                    if (uiState.isExporting) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pageCount = uiState.document?.pages?.size ?: 1
                                if (pageCount > 1 && uiState.selectedFormat == ExportFormat.PDF) {
                                    LinearProgressIndicator(
                                        progress = uiState.exportProgress,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        "Đang xuất ${(uiState.exportProgress * pageCount).toInt()}/$pageCount trang...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    CircularProgressIndicator()
                                    Text("Đang xuất...", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportAndSave(fallbackBitmap) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.SaveAlt, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lưu vào thiết bị", style = MaterialTheme.typography.bodyLarge)
                            }

                            OutlinedButton(
                                onClick = { viewModel.saveToPhotos(fallbackBitmap) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Photo, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lưu vào Thư viện ảnh", style = MaterialTheme.typography.bodyLarge)
                            }

                            OutlinedButton(
                                onClick = { viewModel.share(fallbackBitmap) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Chia sẻ", style = MaterialTheme.typography.bodyLarge)
                            }

                            TextButton(
                                onClick = onDone,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Về trang chủ")
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun FormatCard(
    format: ExportFormat,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (format) {
        ExportFormat.PDF -> Icons.Default.PictureAsPdf
        ExportFormat.JPG -> Icons.Default.Image
        ExportFormat.PNG -> Icons.Default.Image
    }
    val desc = when (format) {
        ExportFormat.PDF -> "Đa trang"
        ExportFormat.JPG -> "Ảnh nén"
        ExportFormat.PNG -> "Ảnh gốc"
    }

    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            format.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            desc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
