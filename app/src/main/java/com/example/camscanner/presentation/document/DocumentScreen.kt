package com.example.camscanner.presentation.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.camscanner.domain.model.Page
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    documentId: String,
    onAddPage: (String) -> Unit = {},
    onEditPage: (documentId: String, pageId: String) -> Unit = { _, _ -> },
    onExport: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(documentId) { viewModel.loadDocument(documentId) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    if (uiState.showRenameDialog) {
        RenameDialog(
            currentName = uiState.document?.name ?: "",
            onConfirm = { viewModel.renameDocument(it) },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }

    uiState.deleteConfirmPageId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeletePage() },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Xóa trang?") },
            text = { Text("Trang này sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeletePage() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Xóa") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelDeletePage() }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.document?.name ?: "Tài liệu",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        uiState.document?.let { doc ->
                            val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                            Text(
                                "${doc.pages.size} trang · ${fmt.format(Date(doc.updatedAt))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showRenameDialog() }) {
                        Icon(Icons.Default.DriveFileRenameOutline, "Đổi tên")
                    }
                    IconButton(onClick = { uiState.document?.id?.let { onExport(it) } }) {
                        Icon(Icons.Default.IosShare, "Xuất")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { uiState.document?.id?.let { onAddPage(it) } },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Thêm trang") }
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
            uiState.document == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy tài liệu", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val pages = uiState.document!!.pages.sortedBy { it.order }
                if (pages.isEmpty()) {
                    EmptyPagesState(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        onAddPage = { uiState.document?.id?.let { onAddPage(it) } }
                    )
                } else {
                    ReorderablePageList(
                        pages = pages,
                        modifier = Modifier.fillMaxSize().padding(padding),
                        onReorder = { reorderedIds -> viewModel.reorderPages(reorderedIds) },
                        onEdit = { page -> onEditPage(documentId, page.id) },
                        onDelete = { page -> viewModel.requestDeletePage(page.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderablePageList(
    pages: List<Page>,
    modifier: Modifier = Modifier,
    onReorder: (List<String>) -> Unit,
    onEdit: (Page) -> Unit,
    onDelete: (Page) -> Unit
) {
    var localPages by remember(pages) { mutableStateOf(pages) }
    val lazyListState = rememberLazyListState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localPages = localPages.toMutableList().apply { add(to.index, removeAt(from.index)) }
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        onReorder(localPages.map { it.id })
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(localPages, key = { _, page -> page.id }) { index, page ->
            ReorderableItem(reorderState, key = page.id) { isDragging ->
                PageItem(
                    page = page,
                    index = index,
                    isDragging = isDragging,
                    scope = this,
                    onEdit = { onEdit(page) },
                    onDelete = { onDelete(page) }
                )
            }
        }
    }
}

@Composable
private fun PageItem(
    page: Page,
    index: Int,
    isDragging: Boolean,
    scope: sh.calvin.reorderable.ReorderableCollectionItemScope,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation = if (isDragging) 8.dp else 1.dp
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            IconButton(
                onClick = {},
                modifier = with(scope) { Modifier.draggableHandle() }.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Kéo để sắp xếp",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            // Thumbnail
            val imageFile = File(page.previewPath)
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Trang ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.Center).size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Trang ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                val filterLabel = when (page.filter.name) {
                    "BW" -> "Đen trắng"; "COLOR" -> "Màu sắc"; "DARK" -> "Tối"; else -> "Gốc"
                }
                Text(
                    filterLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            FilledTonalIconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, "Chỉnh sửa", modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.DeleteOutline, "Xóa",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyPagesState(modifier: Modifier = Modifier, onAddPage: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(12.dp))
            Text("Tài liệu chưa có trang nào", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "Nhấn nút bên dưới để thêm trang đầu tiên",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAddPage, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Thêm trang")
            }
        }
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DriveFileRenameOutline, null) },
        title = { Text("Đổi tên tài liệu") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên tài liệu") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Lưu") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
