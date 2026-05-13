# UI Parity Rules — Android / iOS

## Mục tiêu

Android và iOS phải có **cùng trải nghiệm người dùng** — cùng visual structure, cùng behavior, cùng flow. Người dùng chuyển từ Android sang iOS (hoặc ngược lại) không được cảm thấy đang dùng app khác.

---

## Những gì PHẢI đồng nhất

### Visual
- Cùng layout structure (vị trí các element, hierarchy)
- Cùng spacing và sizing (dùng logical unit — dp/pt từ Figma)
- Cùng color scheme (từ shared tokens)
- Cùng typography scale (font size, weight, line height từ tokens)
- Cùng border radius
- Cùng icon set (hoặc icon tương đương về visual)
- Cùng empty state, loading state, error state

### Behavior
- Cùng luồng điều hướng (navigation flow)
- Cùng tên màn hình và tên component
- Cùng trạng thái (states): default, pressed, focused, disabled, loading, error
- Cùng animation intent (có thể khác implementation nhưng phải cùng cảm giác)
- Cùng feedback khi thao tác (success/error message, toast/snackbar)
- Cùng validation rules
- Cùng permission flow (trừ native dialog)
- Cùng offline behavior

### Data & Logic
- Cùng tên field, cùng format dữ liệu hiển thị
- Cùng format ngày giờ
- Cùng label và copywriting

---

## Những khác biệt được CHẤP NHẬN

| Khác biệt | Android | iOS | Ghi chú |
|---|---|---|---|
| Permission dialog | Native Android dialog | Native iOS dialog | Không custom — dùng system dialog |
| Share sheet | Android Sharesheet | iOS Share Sheet | Không custom — dùng system share |
| Safe area | WindowInsets | SafeAreaInsets | Handle theo platform |
| Status bar | Transparent / colored | Light/dark content | Theo platform convention |
| Navigation bar | System nav bar (gesture/button) | Home indicator | Handle theo platform |
| File picker | SAF / MediaStore | UIDocumentPickerViewController | Không custom |
| Font rendering | Roboto (default) | SF Pro (default) | Khác biệt nhỏ do platform font |
| Scroll physics | Android fling | iOS momentum scroll | Theo platform default |
| Back gesture | Android back gesture/button | iOS swipe back | Theo platform convention |
| Haptic feedback | Android Vibrator API | iOS UIFeedbackGenerator | Cùng intent, khác implementation |
| Keyboard behavior | Android IME | iOS keyboard | Theo platform default |

---

## Định nghĩa "Pixel-Perfect"

**Pixel-perfect trong context này KHÔNG có nghĩa là match từng physical pixel.**

Pixel-perfect = **match theo logical unit của Figma**:
- 1 Figma unit = 1 Android dp = 1 iOS pt
- Không target physical pixel của thiết bị cụ thể
- Không cần match trên mọi screen density

### Acceptance criteria khi so sánh screenshot

| Thuộc tính | Tolerance | Ghi chú |
|---|---|---|
| Spacing / padding | ±0dp (phải match spec) | Fixed — không có tolerance |
| Font size | ±0sp (phải match spec) | Fixed |
| Icon size | ±0dp (phải match spec) | Fixed |
| Border radius | ±0dp (phải match spec) | Fixed |
| Color | ±0 (phải match hex) | Trừ anti-aliasing edge |
| Shadow / elevation | Visual hierarchy phải đúng | Không cần match exact pixel |
| Font rendering | Cho phép khác biệt nhỏ | Do platform font engine |
| Anti-aliasing | Cho phép khác biệt | Do platform renderer |
| Animation timing | ±50ms | Cùng intent là đủ |

---

## Component Naming Parity

Cùng tên component trên cả 2 platform:

| Tên chung | Android (Compose) | iOS (tương lai) |
|---|---|---|
| AppTopBar | `AppTopBar` composable | `AppTopBar` view |
| PrimaryButton | `PrimaryButton` composable | `PrimaryButton` view |
| SecondaryButton | `SecondaryButton` composable | `SecondaryButton` view |
| IconButton | `IconButton` composable | `IconButton` view |
| DocumentCard | `DocumentCard` composable | `DocumentCard` view |
| EmptyState | `EmptyState` composable | `EmptyState` view |
| LoadingState | `LoadingState` composable | `LoadingState` view |
| ErrorState | `ErrorState` composable | `ErrorState` view |
| BottomActionBar | `BottomActionBar` composable | `BottomActionBar` view |
| ScanCaptureButton | `ScanCaptureButton` composable | `ScanCaptureButton` view |
| PageThumbnail | `PageThumbnail` composable | `PageThumbnail` view |
| FilterChip | `FilterChip` composable | `FilterChip` view |
| PermissionView | `PermissionView` composable | `PermissionView` view |

---

## Screen Naming Parity

| Tên chung | Android route | iOS (tương lai) |
|---|---|---|
| HomeScreen | `home` | `HomeView` |
| CameraScreen | `camera/{documentId?}` | `CameraView` |
| DetectionScreen | `detection/{imageUri}` | `DetectionView` |
| EditScreen | `edit/{pageId}` | `EditView` |
| DocumentScreen | `document/{documentId}` | `DocumentView` |
| ExportScreen | `export/{documentId}` | `ExportView` |
| SettingsScreen | `settings` | `SettingsView` |

---

## Quy trình kiểm tra parity

1. Implement Android trước.
2. Khi implement iOS, so sánh từng màn hình với screen spec (`design/screens/*.md`).
3. Mọi khác biệt phải được document trong screen spec hoặc component spec.
4. Khác biệt không có trong danh sách "được chấp nhận" ở trên → phải fix.
5. Dùng `design/screenshots/` để lưu reference screenshots cho cả 2 platform.
