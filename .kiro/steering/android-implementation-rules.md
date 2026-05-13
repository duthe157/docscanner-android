# Android Implementation Rules — CamScanner App

## Xác nhận stack hiện tại

Project đang dùng **Jetpack Compose** (đã xác nhận qua `build.gradle.kts`):
- `buildFeatures { compose = true }`
- `composeOptions { kotlinCompilerExtensionVersion = "1.5.4" }`
- Material3: `androidx.compose.material3:material3`

Không dùng XML layout. Toàn bộ UI là Compose.

---

## Cấu trúc Composable

### Route Composable (trong NavGraph)
- Inject ViewModel qua `hiltViewModel()`
- Collect state qua `collectAsStateWithLifecycle()`
- Handle side effects (navigation, one-time events)
- Truyền state và callbacks xuống Screen Composable

```kotlin
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToCamera -> onNavigateToCamera()
                is HomeEvent.NavigateToDocument -> onNavigateToDocument(event.documentId)
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onScanClick = viewModel::onScanClick,
        onDocumentClick = viewModel::onDocumentClick,
        onSettingsClick = onNavigateToSettings
    )
}
```

### Screen Composable
- Nhận `uiState` và callbacks — không inject ViewModel
- Mostly stateless — chỉ có UI state nhỏ (scroll position, animation state)
- Xử lý tất cả states: loading, empty, content, error

```kotlin
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onScanClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> LoadingState()
        uiState.error != null -> ErrorState(
            message = uiState.error,
            onRetry = { /* retry action */ }
        )
        uiState.documents.isEmpty() -> EmptyState(
            title = "Chưa có tài liệu nào",
            subtitle = "Nhấn Scan để bắt đầu scan tài liệu đầu tiên",
            actionLabel = "Scan ngay",
            onAction = onScanClick
        )
        else -> HomeContent(
            documents = uiState.documents,
            onDocumentClick = onDocumentClick
        )
    }
}
```

### Component Composable
- Stateless hoặc minimal UI state
- Nhận `modifier: Modifier = Modifier` parameter
- Áp dụng modifier lên root element

---

## ViewModel Pattern

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val manageDocumentUseCase: ManageDocumentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadDocuments()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            manageDocumentUseCase.getAllDocuments()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Có lỗi xảy ra. Thử lại.") }
                }
                .collect { documents ->
                    _uiState.update { it.copy(isLoading = false, documents = documents, error = null) }
                }
        }
    }

    fun onScanClick() {
        viewModelScope.launch {
            _events.send(HomeEvent.NavigateToCamera)
        }
    }
}
```

---

## Theme & Tokens

### Cấu trúc theme (tạo nếu chưa có)
```
presentation/theme/
├── Color.kt        ← Định nghĩa màu từ tokens
├── Typography.kt   ← Định nghĩa typography từ tokens
├── Shape.kt        ← Định nghĩa radius từ tokens
└── Theme.kt        ← MaterialTheme wrapper
```

### Dùng theme trong Composable
```kotlin
// Màu
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface

// Typography
MaterialTheme.typography.titleLarge
MaterialTheme.typography.bodyMedium
MaterialTheme.typography.labelLarge

// Shape
MaterialTheme.shapes.medium   // card, button
MaterialTheme.shapes.large    // dialog, bottom sheet
```

### KHÔNG hardcode
```kotlin
// SAI
Text(color = Color(0xFF1A1A1A), fontSize = 16.sp)
Box(modifier = Modifier.background(Color.White))

// ĐÚNG
Text(color = MaterialTheme.colorScheme.onBackground,
     style = MaterialTheme.typography.bodyMedium)
Box(modifier = Modifier.background(MaterialTheme.colorScheme.background))
```

---

## Đơn vị đo lường

```kotlin
// Layout — dùng dp
Modifier.padding(16.dp)
Modifier.size(48.dp)
Modifier.height(56.dp)

// Text — dùng sp
fontSize = 16.sp
lineHeight = 24.sp

// KHÔNG dùng px
// Modifier.padding(48.px)  ← SAI
```

---

## Safe Area / WindowInsets

```kotlin
// Trong Activity
WindowCompat.setDecorFitsSystemWindows(window, false)

// Trong Scaffold
Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing
) { paddingValues ->
    Content(modifier = Modifier.padding(paddingValues))
}

// Camera screen — edge-to-edge
Scaffold(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.systemBars)
)
```

---

## Image Loading (Coil)

```kotlin
// Thumbnail trong list
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(page.previewPath)
        .crossfade(true)
        .build(),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(8.dp))
)
```

---

## Permissions (Accompanist)

```kotlin
val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

when (cameraPermissionState.status) {
    is PermissionStatus.Granted -> CameraContent()
    is PermissionStatus.Denied -> PermissionView(
        title = "Cần quyền truy cập camera",
        message = "Vào Cài đặt để cấp quyền.",
        onOpenSettings = { /* open app settings */ }
    )
}
```

---

## Giữ nguyên business logic

Khi cập nhật UI, **không chạm vào**:
- `domain/usecase/` — UseCase logic
- `domain/repository/` — Repository interfaces
- `data/` — Data layer (Room, FileStorage, TFLite, OpenCV)
- `util/ImageUtils.kt`, `util/PerspectiveTransform.kt` — Image processing

Chỉ thay đổi:
- `presentation/*/` — Screens, ViewModels, Components
- `presentation/theme/` — Theme, Colors, Typography, Shapes
- `res/` — Drawables, strings (nếu cần)

---

## Build & Verify

Sau mỗi thay đổi UI:
1. Build project: `./gradlew assembleDebug`
2. Kiểm tra không có compile error
3. Chạy app trên emulator/device
4. Kiểm tra visual theo `qa-visual-rules.md`
5. Kiểm tra không có crash khi navigate qua các màn hình
