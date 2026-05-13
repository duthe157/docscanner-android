# Android Current UI Mapping Report

> **Cập nhật**: Sau khi implement dark theme + new HomeScreen layout
> **Trạng thái Android**: Theme ✅ | HomeScreen ✅ | Các screens còn lại: dùng theme nhưng chưa update layout

---

## 1. Công nghệ UI Android hiện tại

### UI Framework
- **Jetpack Compose** 100% — không có XML layout
- **Material3** dark color scheme — custom theme đã implement
- **Navigation Compose** 2.7.5

### Theme System — ĐÃ IMPLEMENT ✅

```
presentation/theme/
├── Color.kt        ✅ — dark palette đầy đủ
├── Typography.kt   ✅ — 11 text styles
├── Shape.kt        ✅ — 5 radius levels
└── Theme.kt        ✅ — CamScannerTheme wrapping MaterialTheme
```

**Màu chính đang dùng:**
- Background: `#0D1F1E`
- Surface/Card: `#1A2E2D`
- Elevated: `#1F3533`
- Bottom Nav: `#111E1D`
- Primary/Teal: `#2DD4BF`
- Container: `#1A3D3A`

### Navigation Routes (thực tế)

| Screen | Route thực tế |
|---|---|
| Home | `home` |
| Camera | `camera/{documentId}` |
| Detection | `detection/{imageUri}/{documentId}` |
| Edit | `edit/{documentId}/{pageId}` |
| Document | `document/{documentId}` |
| Export | `export/{documentId}` |
| Settings | `settings` |

**Lưu ý**: `ScanSession` object truyền Bitmap/corners giữa Detection → Edit — không qua nav args.

---

## 2. Trạng thái từng Screen

| Screen | File | Layout update | Theme | iOS spec |
|---|---|---|---|---|
| HomeScreen | `home/HomeScreen.kt` | ✅ New layout (tabs, quick actions, recent, storage) | ✅ | `design/screens/home.md` |
| CameraScreen | `camera/CameraScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/scan-camera.md` |
| DetectionScreen | `detection/DetectionScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/crop-document.md` |
| EditScreen | `edit/EditScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/preview-document.md` |
| DocumentScreen | `document/DocumentScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/document-detail.md` |
| ExportScreen | `export/ExportScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/export-share.md` |
| SettingsScreen | `settings/SettingsScreen.kt` | ⚠️ Chưa update layout | ✅ theme applied | `design/screens/settings.md` |

---

## 3. HomeScreen — Chi tiết implementation

### Cấu trúc hiện tại (đã implement)

```kotlin
HomeScreen
├── Dialogs (delete confirm, merge picker)
├── Scaffold
│   ├── bottomBar: AppBottomNav (3 tabs)
│   └── content:
│       ├── Tab 0: HomeTab
│       │   ├── Title "CamScanner" (centered, headlineMedium)
│       │   ├── Section "Thao tác nhanh"
│       │   │   └── Row: [Import][Scan][Dán][Thư viện]
│       │   ├── Section "Tài liệu gần đây" + "Xem tất cả"
│       │   │   └── LazyRow: RecentDocumentCard × 4
│       │   ├── Section "Tổng quan"
│       │   │   └── StorageOverviewCard (docs count + pages count)
│       │   ├── Section "Tất cả tài liệu"
│       │   └── LazyColumn: DocumentItem × N
│       └── Tab 1: LibraryTab
│           ├── TopAppBar "Thư viện" + search icon
│           ├── LazyColumn: DocumentItem × N
│           └── FAB (+) teal
```

### AppBottomNav
- Container: `BackgroundBottomNav` = `#111E1D`
- Tonal elevation: 0dp
- Selected: icon filled, color `#2DD4BF`, indicator `#1A3D3A`
- Unselected: icon outlined, color `#8A9E9D`
- Tab 2 (Settings) → `onSettingsClick()` không switch tab

### QuickActionButton
- Icon box: 56dp, radius 12dp, bg `#1A3D3A`
- Icon: 24dp, color `#2DD4BF`
- Label: 11sp, color `#8A9E9D`
- Padding H: 8dp, V: 4dp, gap: 6dp

### RecentDocumentCard
- Width: 100dp, thumbnail height: 80dp
- Padding: 8dp, radius: 12dp
- Border: 1dp `#243534`
- Bg: `#1A2E2D`

### StorageOverviewCard
- Padding: 16dp, radius: 12dp, border: 1dp `#243534`
- Divider: 1dp × 48dp
- Value font: 16sp Medium, color `#FFFFFF`
- Label font: 11sp, color `#8A9E9D`

### DocumentItem (dùng ở cả 2 tab)
- Card bg: `#1A2E2D`, radius: 12dp, border: 1dp `#243534`
- Padding: 12dp
- Thumbnail: 52×70dp, radius 8dp, bg `#1F3533`
- Name: 14sp Medium
- Meta: 11sp, color `#8A9E9D`
- More menu: Hợp nhất + Xóa (error color)

---

## 4. Những phần KHÔNG được thay đổi

### Business Logic
| File | Lý do |
|---|---|
| `util/ImageUtils.kt` | Filter, sharpen, denoise, rotate |
| `util/PerspectiveTransform.kt` | OpenCV warp |
| `domain/usecase/` | Tất cả use cases |
| `data/` | Toàn bộ data layer |
| `di/` | Hilt modules |

### Navigation & Session
| Code | Lý do |
|---|---|
| `ScanSession` object | Truyền Bitmap giữa screens |
| `ScanSession.pendingImportPaths` | Multi-image import queue |
| `NavGraph.kt` callbacks | Back stack management |
| `popUpTo` logic | Đảm bảo back stack đúng |

---

## 5. iOS → Android Mapping

Khi implement iOS, map 1-1 theo bảng này:

| Android Composable | iOS SwiftUI | Notes |
|---|---|---|
| `Scaffold` | `NavigationStack` + `ZStack` | — |
| `TopAppBar` | `NavigationBar` / custom toolbar | — |
| `NavigationBar` (bottom) | `TabView` hoặc custom `HStack` | bg = `#111E1D` |
| `LazyColumn` | `LazyVStack` trong `ScrollView` hoặc `List` | — |
| `LazyRow` | `ScrollView(.horizontal)` + `LazyHStack` | — |
| `Card` | `RoundedRectangle` + `.fill` + `.stroke` | — |
| `FloatingActionButton` | Custom `Button` overlay | — |
| `ExtendedFloatingActionButton` | Custom `Button` với label | — |
| `CircularProgressIndicator` | `ProgressView()` | — |
| `LinearProgressIndicator` | `ProgressView(value:)` `.progressViewStyle(.linear)` | — |
| `AlertDialog` | `.alert` modifier | — |
| `DropdownMenu` | `Menu` button | — |
| `OutlinedTextField` | `TextField` với `.roundedBorder` | — |
| `FilterChip` | Custom toggle button | — |
| `Slider` | `Slider` | — |
| `AsyncImage` (Coil) | `AsyncImage` (SwiftUI native) | iOS 15+ |
| `Canvas` | `Canvas` (SwiftUI) | — |
| `AndroidView` (CameraX) | `UIViewRepresentable` (AVFoundation) | — |
| `rememberPermissionState` | `AVCaptureDevice.requestAccess` | — |
| `SharedPreferences` | `UserDefaults` | — |
| `Intent.ACTION_SEND` | `UIActivityViewController` | — |
| `MediaStore` | `PHPhotoLibrary` | — |
| `ReorderableItem` | `List` với `.onMove` | — |
| `hiltViewModel()` | `@StateObject` / `@EnvironmentObject` | — |
| `collectAsState()` | `@Published` + `@ObservedObject` | — |
| `LaunchedEffect` | `.onAppear` / `.task` | — |
| `WindowInsets` | `.safeAreaInset` / `GeometryReader` | — |
| `statusBarsPadding()` | `.safeAreaInset(edge: .top)` | — |
| `navigationBarsPadding()` | `.safeAreaInset(edge: .bottom)` | — |
