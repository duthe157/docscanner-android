package com.example.camscanner.presentation.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.camscanner.domain.model.Document
import com.example.camscanner.presentation.navigation.ScanSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onScanClick: () -> Unit = {},
    onImportImage: (String) -> Unit = {},
    onDocumentClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var isImporting by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
            // Copy all selected images to cache, then navigate to first one
            // Subsequent images queued via ScanSession.pendingImportPaths
            val paths = withContext(Dispatchers.IO) {
                uris.mapIndexed { i, uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: Exception) {}
                    val cacheFile = File(context.cacheDir, "import_${System.currentTimeMillis()}_$i.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        cacheFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    cacheFile.absolutePath
                }
            }
            isImporting = false
            if (paths.isNotEmpty()) {
                // Store remaining paths for sequential processing
                ScanSession.pendingImportPaths = paths.drop(1).toMutableList()
                onImportImage(paths.first())
            }
        }
    }

    // Delete confirm dialog
    uiState.deleteConfirmDocumentId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Xóa tài liệu?") },
            text = { Text("Tài liệu và tất cả trang sẽ bị xóa vĩnh viễn.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Xóa") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.cancelDelete() }) { Text("Hủy") }
            }
        )
    }

    // Merge target picker (Req 6.7)
    if (uiState.showMergeTargetPicker) {
        val sourceId = uiState.mergeSourceId
        val targets = uiState.documents.filter { it.id != sourceId }
        AlertDialog(
            onDismissRequest = { viewModel.cancelMerge() },
            icon = { Icon(Icons.Default.MergeType, null) },
            title = { Text("Hợp nhất vào tài liệu nào?") },
            text = {
                Column {
                    Text(
                        "Chọn tài liệu đích. Tài liệu nguồn sẽ bị xóa sau khi hợp nhất.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))
                    targets.forEach { doc ->
                        OutlinedButton(
                            onClick = { viewModel.confirmMerge(doc.id) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(doc.name, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.cancelMerge() }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CamScanner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Cài đặt")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Description, null) },
                    label = { Text("Tài liệu") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onScanClick,
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Quét",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    label = { Text("Quét") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Cài đặt") }
                )
            }
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = { imageLauncher.launch(arrayOf("image/*")) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Image, "Import ảnh")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.documents.isEmpty() -> {
                EmptyHomeState(modifier = Modifier.fillMaxSize().padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.documents, key = { it.id }) { doc ->
                        DocumentItem(
                            document = doc,
                            onClick = { onDocumentClick(doc.id) },
                            onDelete = { viewModel.requestDeleteDocument(doc.id) },
                            onMerge = { viewModel.startMerge(doc.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMerge: () -> Unit = {}
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()) }
    val firstPage = document.pages.minByOrNull { it.order }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail — portrait ratio
            val thumbFile = firstPage?.previewPath?.let { File(it) }
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (thumbFile != null && thumbFile.exists()) {
                    AsyncImage(
                        model = thumbFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.Center).size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    document.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Article,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        "${document.pages.size} trang",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    dateFmt.format(Date(document.updatedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Thêm",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Hợp nhất tài liệu") },
                    leadingIcon = { Icon(Icons.Default.MergeType, null) },
                    onClick = { showMenu = false; onMerge() }
                )
                DropdownMenuItem(
                    text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}

@Composable
private fun EmptyHomeState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DocumentScanner,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Chưa có tài liệu nào",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Nhấn nút Quét để bắt đầu\nhoặc import ảnh từ thư viện",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
