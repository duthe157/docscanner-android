package com.example.camscanner.presentation.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.camscanner.presentation.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen — entry point, manages dialogs + tab state
// ─────────────────────────────────────────────────────────────────────────────

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

    // ── Image import launcher (Req 2.3: multi-import queue) ──────────────────
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            isImporting = true
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
                ScanSession.pendingImportPaths = paths.drop(1).toMutableList()
                onImportImage(paths.first())
            }
        }
    }

    // ── Delete confirm dialog ─────────────────────────────────────────────────
    uiState.deleteConfirmDocumentId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
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

    // ── Merge target picker (Req 6.7) ─────────────────────────────────────────
    if (uiState.showMergeTargetPicker) {
        val sourceId = uiState.mergeSourceId
        val targets = uiState.documents.filter { it.id != sourceId }
        AlertDialog(
            onDismissRequest = { viewModel.cancelMerge() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            icon = { Icon(Icons.Default.MergeType, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Hợp nhất vào tài liệu nào?") },
            text = {
                Column {
                    Text(
                        "Chọn tài liệu đích. Tài liệu nguồn sẽ bị xóa sau khi hợp nhất.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    targets.forEach { doc ->
                        OutlinedButton(
                            onClick = { viewModel.confirmMerge(doc.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                doc.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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

    // ── Main scaffold ─────────────────────────────────────────────────────────
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomNav(
                selectedTab = selectedTab,
                onTabChange = { tab ->
                    if (tab == 2) {
                        onSettingsClick()
                    } else {
                        selectedTab = tab
                    }
                }
            )
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTab(
                uiState = uiState,
                onScanClick = onScanClick,
                onImportClick = { imageLauncher.launch(arrayOf("image/*")) },
                onDocumentClick = onDocumentClick,
                onSwitchToLibrary = { selectedTab = 1 },
                isImporting = isImporting,
                onDelete = { viewModel.requestDeleteDocument(it) },
                onMerge = { viewModel.startMerge(it) },
                modifier = Modifier.padding(padding)
            )
            1 -> LibraryTab(
                uiState = uiState,
                onDocumentClick = onDocumentClick,
                onScanClick = onScanClick,
                isImporting = isImporting,
                onDelete = { viewModel.requestDeleteDocument(it) },
                onMerge = { viewModel.startMerge(it) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AppBottomNav — 3 tabs: Home, Library, Settings
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppBottomNav(
    selectedTab: Int,
    onTabChange: (Int) -> Unit
) {
    NavigationBar(
        containerColor = BackgroundBottomNav,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabChange(0) },
            icon = {
                Icon(
                    if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Trang chủ"
                )
            },
            label = { Text("Trang chủ") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = TealContainer
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabChange(1) },
            icon = {
                Icon(
                    if (selectedTab == 1) Icons.Filled.Folder else Icons.Outlined.Folder,
                    contentDescription = "Thư viện"
                )
            },
            label = { Text("Thư viện") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = TealContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { onTabChange(2) },
            icon = {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Cài đặt"
                )
            },
            label = { Text("Cài đặt") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = TealContainer
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HomeTab — Quick Actions + Recent Documents + Storage Overview + full list
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTab(
    uiState: HomeUiState,
    onScanClick: () -> Unit,
    onImportClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSwitchToLibrary: () -> Unit,
    isImporting: Boolean,
    onDelete: (String) -> Unit = {},
    onMerge: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Title ─────────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CamScanner",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Quick Actions ─────────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Thao tác nhanh",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Outlined.FileUpload,
                    label = "Import",
                    onClick = onImportClick,
                    isLoading = isImporting
                )
                QuickActionButton(
                    icon = Icons.Outlined.CameraAlt,
                    label = "Scan",
                    onClick = onScanClick
                )
                QuickActionButton(
                    icon = Icons.Outlined.ContentPaste,
                    label = "Dán",
                    onClick = { /* paste action — placeholder */ }
                )
                QuickActionButton(
                    icon = Icons.Outlined.Folder,
                    label = "Thư viện",
                    onClick = onSwitchToLibrary
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Recent Documents ──────────────────────────────────────────────────
        if (uiState.documents.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tài liệu gần đây",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onSwitchToLibrary) {
                        Text(
                            text = "Xem tất cả",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                val recentDocs = uiState.documents.take(4)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentDocs, key = { it.id }) { doc ->
                        RecentDocumentCard(
                            document = doc,
                            onClick = { onDocumentClick(doc.id) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Storage Overview ──────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Tổng quan",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            StorageOverviewCard(
                totalDocuments = uiState.documents.size,
                totalPages = uiState.documents.sumOf { it.pages.size },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        // ── Full document list ────────────────────────────────────────────────
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (uiState.documents.isEmpty()) {
            item {
                EmptyLibraryState(
                    onScanClick = onScanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                )
            }
        } else {
            item {
                SectionHeader(
                    title = "Tất cả tài liệu",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(uiState.documents, key = { it.id }) { doc ->
                DocumentItem(
                    document = doc,
                    onClick = { onDocumentClick(doc.id) },
                    onDelete = { onDelete(doc.id) },
                    onMerge = { onMerge(doc.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LibraryTab — full document list + FAB
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTab(
    uiState: HomeUiState,
    onDocumentClick: (String) -> Unit,
    onScanClick: () -> Unit,
    isImporting: Boolean,
    onDelete: (String) -> Unit,
    onMerge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thư viện",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { /* search — future */ }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Scan mới")
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.documents.isEmpty() -> {
                EmptyLibraryState(
                    onScanClick = onScanClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.documents, key = { it.id }) { doc ->
                        DocumentItem(
                            document = doc,
                            onClick = { onDocumentClick(doc.id) },
                            onDelete = { onDelete(doc.id) },
                            onMerge = { onMerge(doc.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QuickActionButton — icon + label in a column
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(TealContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RecentDocumentCard — compact card for horizontal scroll
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentDocumentCard(
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstPage = document.pages.minByOrNull { it.order }
    val dateFmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Card(
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            // Thumbnail
            val thumbFile = firstPage?.previewPath?.let { File(it) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
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
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            // Info
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFmt.format(Date(document.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// StorageOverviewCard — total docs + total pages
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StorageOverviewCard(
    totalDocuments: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StorageStat(
                icon = Icons.Outlined.Description,
                value = totalDocuments.toString(),
                label = "Tài liệu"
            )
            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            StorageStat(
                icon = Icons.Outlined.Article,
                value = totalPages.toString(),
                label = "Trang"
            )
        }
    }
}

@Composable
private fun StorageStat(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DocumentItem — card with thumbnail, name, date, more menu
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMerge: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()) }
    val firstPage = document.pages.minByOrNull { it.order }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                    .clip(MaterialTheme.shapes.small)
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
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Article,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${document.pages.size} trang",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateFmt.format(Date(document.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Hợp nhất tài liệu") },
                        leadingIcon = { Icon(Icons.Default.MergeType, null) },
                        onClick = {
                            showMenu = false
                            onMerge()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Xóa",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SectionHeader — reusable section title
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// EmptyLibraryState — empty state for both tabs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyLibraryState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(TealContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Chưa có tài liệu nào",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Nhấn Scan để bắt đầu scan tài liệu đầu tiên",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Scan ngay",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
