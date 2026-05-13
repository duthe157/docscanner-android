# Component Rules — CamScanner App

## Nguyên tắc chung

- UI lặp lại ở 2 màn hình trở lên → **bắt buộc** tách thành reusable component.
- Component phải **stateless** hoặc **minimal state** — không chứa business logic.
- Component nhận data qua props/parameters, emit events qua callbacks.
- Mỗi component phải có `@Preview` annotation (Android) để xem trước trong IDE.
- Mỗi component phải có spec file tương ứng trong `design/components/`.

---

## Cấu trúc bắt buộc của mỗi Component Spec

Mỗi file `design/components/*.md` phải có đầy đủ các section sau:

### 1. Purpose
Mô tả ngắn gọn component này làm gì và dùng ở đâu.

### 2. Props / Data Contract
Danh sách tất cả parameters/props mà component nhận vào:
- Tên prop
- Kiểu dữ liệu
- Bắt buộc hay optional
- Giá trị mặc định (nếu có)
- Mô tả

### 3. Variants
Các biến thể visual của component (ví dụ: size, style, type).

### 4. States
Tất cả các trạng thái mà component có thể ở:

**States bắt buộc cho mọi interactive component:**
- `default` — trạng thái bình thường
- `pressed` — đang được nhấn (ripple/highlight)
- `focused` — đang được focus (keyboard navigation, accessibility)
- `disabled` — không thể tương tác

**States bổ sung tùy component:**
- `loading` — đang xử lý (nếu component có async action)
- `error` — trạng thái lỗi (nếu component có thể fail)
- `empty` — không có dữ liệu (nếu component hiển thị list/content)
- `selected` — đang được chọn (nếu component có selection)
- `active` — đang active (tab, chip đang được chọn)

### 5. Layout Measurements
- Kích thước (width, height, min/max)
- Padding nội bộ
- Spacing giữa các element bên trong
- Tất cả dùng đơn vị dp (Android) / pt (iOS)

### 6. Typography
- Font size, weight, line height cho từng text element trong component
- Tham chiếu đến typography token

### 7. Colors
- Màu cho từng state
- Tham chiếu đến color token

### 8. Accessibility
- Content description cho screen reader
- Minimum touch target (48dp × 48dp)
- Focus order (nếu có nhiều element)
- Semantic role (button, heading, image, etc.)

### 9. Android Implementation Notes
- Composable function signature
- Modifier usage
- Compose-specific behavior

### 10. iOS Implementation Notes (tương lai)
- SwiftUI/UIKit equivalent
- Platform-specific adjustments

### 11. Acceptance Criteria
- Danh sách điều kiện để component được coi là "done"
- Visual checks
- Behavior checks
- Accessibility checks

---

## Danh sách Components bắt buộc

| Component | File spec | Dùng ở màn hình |
|---|---|---|
| AppTopBar | `app-top-bar.md` | Tất cả màn hình có top bar |
| PrimaryButton | `primary-button.md` | Khắp nơi |
| SecondaryButton | `secondary-button.md` | Khắp nơi |
| IconButton | `icon-button.md` | Top bar, toolbar |
| DocumentCard | `document-card.md` | HomeScreen |
| EmptyState | `empty-state.md` | HomeScreen, DocumentScreen |
| LoadingState | `loading-state.md` | Mọi màn hình có async |
| ErrorState | `error-state.md` | Mọi màn hình có thể fail |
| BottomActionBar | `bottom-action-bar.md` | EditScreen, DocumentScreen |
| ScanCaptureButton | `scan-capture-button.md` | CameraScreen |
| PageThumbnail | `page-thumbnail.md` | DocumentScreen, HomeScreen |
| FilterChip | `filter-chip.md` | EditScreen |
| PermissionView | `permission-view.md` | CameraScreen, khi thiếu quyền |

---

## Quy tắc đặt tên

- Component name: `PascalCase` (ví dụ: `DocumentCard`, `ScanCaptureButton`)
- Props: `camelCase` (ví dụ: `documentName`, `onScanClick`)
- Callback props: bắt đầu bằng `on` (ví dụ: `onClick`, `onDismiss`, `onValueChange`)
- Boolean props: bắt đầu bằng `is` hoặc `has` (ví dụ: `isEnabled`, `isLoading`, `hasError`)

---

## Quy tắc về State

### Stateless component (ưu tiên)
```kotlin
@Composable
fun DocumentCard(
    document: Document,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Component với internal UI state (cho phép)
```kotlin
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
// isSelected được quản lý bởi ViewModel, không phải component
```

### KHÔNG được làm trong component
- Không gọi ViewModel trực tiếp trong component
- Không gọi UseCase trong component
- Không làm network/disk I/O trong component
- Không navigate trong component (dùng callback thay thế)

---

## Quy tắc về Modifier

- Mọi Composable component phải nhận `modifier: Modifier = Modifier` parameter.
- Modifier được truyền vào phải áp dụng lên root element của component.
- Không hardcode size trong component nếu có thể — để caller quyết định qua Modifier.

---

## Quy tắc về Preview

Mỗi component phải có ít nhất 2 preview:
1. Preview trạng thái default
2. Preview trạng thái đặc biệt (disabled, loading, error, empty, v.v.)

```kotlin
@Preview(showBackground = true)
@Composable
fun DocumentCardPreview() {
    AppTheme {
        DocumentCard(
            document = sampleDocument,
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentCardLoadingPreview() {
    AppTheme {
        DocumentCard(
            document = sampleDocument,
            isLoading = true,
            onClick = {},
            onLongClick = {}
        )
    }
}
```
