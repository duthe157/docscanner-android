# Screen Rules — CamScanner App

## Nguyên tắc chung

- Mỗi màn hình có file spec riêng trong `design/screens/`.
- Screen Composable phải **mostly stateless** — nhận state từ ViewModel qua parameters.
- Screen không inject ViewModel trực tiếp — Route Composable trong NavGraph làm việc đó.
- Mỗi màn hình phải xử lý đầy đủ tất cả states: loading, empty, content, error.

---

## Cấu trúc bắt buộc của mỗi Screen Spec

Mỗi file `design/screens/*.md` phải có đầy đủ các section sau:

### 1. Figma Reference
```
Figma frame: [TODO: paste Figma frame link]
Figma page: [TODO: page name]
Last synced: [TODO: date]
```

### 2. Screenshot Path
```
Android: design/screenshots/android/{screen-name}.png
iOS:     design/screenshots/ios/{screen-name}.png  (tương lai)
```

### 3. Purpose
Mô tả ngắn gọn màn hình này làm gì trong luồng của app.

### 4. Layout Baseline
- Thiết kế dựa trên màn hình tham chiếu nào (ví dụ: 390×844dp — iPhone 14 equivalent)
- Safe area handling
- Orientation support (portrait only / landscape / both)

### 5. Structure
Mô tả cấu trúc layout tổng thể:
- Top bar (có/không, loại gì)
- Content area (scrollable/fixed)
- Bottom bar / FAB (có/không)
- Overlay elements

### 6. Exact Measurements
Bảng đo lường chi tiết cho từng element:
- Kích thước (width × height)
- Padding / margin
- Spacing giữa elements
- Tất cả dùng dp (Android) / pt (iOS)
- TODO marker nếu chưa có giá trị từ Figma

### 7. Components Used
Danh sách components được dùng trong màn hình này, với link đến component spec.

### 8. States
Mô tả UI cho từng state:

**States bắt buộc:**
- `loading` — màn hình đang tải dữ liệu
- `empty` — không có dữ liệu để hiển thị
- `content` — có dữ liệu, hiển thị bình thường
- `error` — có lỗi xảy ra

**States bổ sung tùy màn hình:**
- `permission_denied` — thiếu quyền (camera, storage)
- `processing` — đang xử lý ảnh
- `saving` — đang lưu

### 9. Behavior
Mô tả behavior chi tiết:
- Các user action và response tương ứng
- Animation / transition
- Scroll behavior
- Keyboard behavior (nếu có input)
- Long press / swipe gestures

### 10. Navigation
- Entry points (từ màn hình nào có thể đến đây)
- Exit points (từ đây có thể đi đến đâu)
- Back behavior (back button / gesture làm gì)
- Deep link (nếu có)

### 11. Acceptance Criteria
Danh sách điều kiện để màn hình được coi là "done":
- Visual checks (layout, spacing, color, typography)
- Behavior checks (tất cả user actions hoạt động đúng)
- State checks (tất cả states hiển thị đúng)
- Navigation checks (vào/ra đúng)
- Accessibility checks

---

## Danh sách Screens

| Màn hình | File spec | Route |
|---|---|---|
| Home | `home.md` | `home` |
| Scan Camera | `scan-camera.md` | `camera/{documentId?}` |
| Crop Document | `crop-document.md` | `detection/{imageUri}` |
| Preview Document | `preview-document.md` | `edit/{pageId}` |
| Document Detail | `document-detail.md` | `document/{documentId}` |
| Export & Share | `export-share.md` | `export/{documentId}` |
| Settings | `settings.md` | `settings` |

---

## Quy tắc về State Management

### ViewModel State
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val documents: List<Document> = emptyList(),
    val error: String? = null
)
```

### Screen nhận state
```kotlin
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onScanClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Route inject ViewModel
```kotlin
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onScanClick = onNavigateToCamera,
        onDocumentClick = onNavigateToDocument,
        onSettingsClick = onNavigateToSettings
    )
}
```

---

## Quy tắc về Safe Area

- Android: Dùng `WindowInsets` để handle status bar và navigation bar.
- Content không được bị che bởi system bars.
- Bottom bar / FAB phải tính thêm navigation bar inset.
- Camera preview phải full screen (edge-to-edge).

---

## Quy tắc về Orientation

- Mặc định: **Portrait only** cho tất cả màn hình.
- Camera screen: có thể xem xét landscape sau nếu cần.
- Không implement landscape nếu chưa có Figma design cho landscape.
