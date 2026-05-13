# iOS Implementation Rules — CamScanner App

## Trạng thái hiện tại

Android đã implement xong với dark theme. iOS cần implement **y hệt** Android.
Tất cả giá trị UI đã được extract vào `design/tokens/ios.tokens.md`.

**Stack**: SwiftUI (target iOS 15+) + MVVM với `ObservableObject`/`@StateObject`

## Nguồn tham chiếu bắt buộc

Trước khi implement bất kỳ màn hình nào, đọc:
1. `design/tokens/ios.tokens.md` — tất cả colors, spacing, radius, typography
2. `design/screens/{screen}.md` — layout, measurements, states, behavior
3. `design/android-current-ui-mapping.md` — mapping Android → iOS

---

## Đơn vị đo lường

- Layout: `pt` (points) — tương đương dp trên Android
- Text: `pt` — tương đương sp trên Android
- **1 Figma unit = 1 iOS pt** (giống Android: 1 Figma unit = 1 Android dp)
- Không target physical pixel — dùng logical unit

```swift
// SwiftUI
.padding(16)          // 16pt
.frame(height: 56)    // 56pt
.font(.system(size: 16))  // 16pt

// UIKit
view.frame.size.height = 56  // 56pt (CGFloat)
```

---

## Shared Design Tokens

Dùng cùng `design/tokens/base.tokens.json` làm source of truth.

Mapping sang iOS:
- `color.*` → SwiftUI `Color` / UIKit `UIColor`
- `spacing.*` → CGFloat constants
- `radius.*` → `cornerRadius` CGFloat
- `typography.*` → `Font` / `UIFont`
- `shadow.*` → `shadow()` modifier / `layer.shadowRadius`

Xem chi tiết: `design/tokens/ios.tokens.md`

---

## Cấu trúc file iOS (SwiftUI — dự kiến)

```
CamScanner/
├── Presentation/
│   ├── Home/
│   │   ├── HomeView.swift          ← View (tương đương Screen Composable)
│   │   └── HomeViewModel.swift     ← ObservableObject (tương đương ViewModel)
│   ├── Camera/
│   │   ├── CameraView.swift
│   │   └── CameraViewModel.swift
│   ├── Detection/
│   │   ├── DetectionView.swift
│   │   └── DetectionViewModel.swift
│   ├── Edit/
│   │   ├── EditView.swift
│   │   └── EditViewModel.swift
│   ├── Document/
│   │   ├── DocumentView.swift
│   │   └── DocumentViewModel.swift
│   ├── Export/
│   │   ├── ExportView.swift
│   │   └── ExportViewModel.swift
│   ├── Settings/
│   │   ├── SettingsView.swift
│   │   └── SettingsViewModel.swift
│   ├── Components/             ← Reusable components
│   │   ├── AppTopBar.swift
│   │   ├── DocumentCard.swift
│   │   └── ...
│   └── Theme/
│       ├── Colors.swift
│       ├── Typography.swift
│       └── Shapes.swift
├── Domain/
│   ├── Model/
│   └── UseCase/
└── Data/
    ├── Local/
    └── ML/
```

---

## Component Naming Parity

Giữ cùng tên component với Android:

| Android (Compose) | iOS (SwiftUI) |
|---|---|
| `AppTopBar` | `AppTopBar` |
| `PrimaryButton` | `PrimaryButton` |
| `SecondaryButton` | `SecondaryButton` |
| `IconButton` | `IconButton` |
| `DocumentCard` | `DocumentCard` |
| `EmptyState` | `EmptyState` |
| `LoadingState` | `LoadingState` |
| `ErrorState` | `ErrorState` |
| `BottomActionBar` | `BottomActionBar` |
| `ScanCaptureButton` | `ScanCaptureButton` |
| `PageThumbnail` | `PageThumbnail` |
| `FilterChip` | `FilterChip` |
| `PermissionView` | `PermissionView` |

---

## Screen Naming Parity

| Android route | iOS View |
|---|---|
| `HomeScreen` | `HomeView` |
| `CameraScreen` | `CameraView` |
| `DetectionScreen` | `DetectionView` |
| `EditScreen` | `EditView` |
| `DocumentScreen` | `DocumentView` |
| `ExportScreen` | `ExportView` |
| `SettingsScreen` | `SettingsView` |

---

## Safe Area

```swift
// SwiftUI — tự động handle safe area
VStack {
    content
}
.ignoresSafeArea(.container, edges: .top)  // Camera screen

// UIKit
view.safeAreaInsets  // Đọc insets thủ công
```

---

## Platform-Specific Differences

### Navigation
- Android: Navigation Compose với back stack
- iOS: NavigationStack (SwiftUI) hoặc UINavigationController (UIKit)
- Behavior phải tương đương — xem `flow-rules.md` cho back behavior

### Camera
- Android: CameraX
- iOS: AVFoundation / AVCaptureSession
- Behavior phải tương đương — xem `design/screens/scan-camera.md`

### Image Picker
- Android: ActivityResultContracts + MediaStore
- iOS: PHPickerViewController
- Behavior phải tương đương — xem `design/flows/import-image-to-pdf.md`

### Share Sheet
- Android: Intent.ACTION_SEND
- iOS: UIActivityViewController
- Không custom — dùng system share sheet

### Permission
- Android: Accompanist Permissions
- iOS: AVCaptureDevice.requestAccess + PHPhotoLibrary.requestAuthorization
- Behavior phải tương đương — xem `flow-rules.md`

### Storage
- Android: Room (SQLite) + FileSystem
- iOS: Core Data (SQLite) + FileManager
- Schema phải tương đương — xem `design.md` của từng feature spec

### PDF Export
- Android: android.graphics.pdf.PdfDocument
- iOS: PDFKit hoặc UIGraphicsPDFRenderer
- Output phải tương đương

---

## Những khác biệt được document

Mọi khác biệt giữa Android và iOS phải được ghi rõ trong:
- Screen spec (`design/screens/*.md`) — section "Platform Notes"
- Component spec (`design/components/*.md`) — section "iOS Implementation Notes"
- Flow spec (`design/flows/*.md`) — section "Android / iOS Parity Notes"

Khác biệt không được document → coi là bug, phải fix.

---

## Checklist trước khi implement iOS

### Setup
- [ ] Tạo `Theme/Colors.swift` từ `design/tokens/ios.tokens.md`
- [ ] Tạo `Theme/Typography.swift` từ `design/tokens/ios.tokens.md`
- [ ] Tạo `Theme/Spacing.swift` từ `design/tokens/ios.tokens.md`
- [ ] Tạo `Theme/Shapes.swift` từ `design/tokens/ios.tokens.md`

### Mỗi màn hình
- [ ] Đọc `design/screens/{screen}.md` — layout, measurements, states
- [ ] Đọc `design/components/*.md` cho components cần dùng
- [ ] Đọc `design/android-current-ui-mapping.md` — iOS mapping table
- [ ] Implement theo đúng measurements trong spec
- [ ] Test tất cả states: loading, empty, content, error
- [ ] So sánh với Android screenshot
- [ ] Chạy QA visual theo `qa-visual-rules.md`

### Thứ tự implement (từ dễ đến khó)
1. `SettingsView` — ít logic nhất
2. `HomeView` — 2 tabs, quick actions, recent docs, storage
3. `DocumentView` — list với drag-to-reorder
4. `ExportView` — format selector, export actions
5. `EditView` — filter chips, sliders, preview
6. `DetectionView` — drag handles, canvas overlay
7. `CameraView` — AVFoundation, guide frame overlay

## Dark Theme — Bắt buộc

App **chỉ có dark theme**. Không implement light mode.

```swift
// Trong App entry point
.preferredColorScheme(.dark)
// Hoặc set trong Info.plist:
// UIUserInterfaceStyle = Dark
```

## Màu nền bắt buộc

Tất cả screens phải có background `#0D1F1E`:
```swift
.background(Color.backgroundPrimary)
.ignoresSafeArea()
```

## Bottom Navigation

```swift
// Tab bar background
init() {
    let appearance = UITabBarAppearance()
    appearance.backgroundColor = UIColor(Color.backgroundBottomNav) // #111E1D
    UITabBar.appearance().standardAppearance = appearance
    UITabBar.appearance().scrollEdgeAppearance = appearance
}

TabView(selection: $selectedTab) {
    HomeTabView().tabItem { Label("Trang chủ", systemImage: "house") }.tag(0)
    LibraryTabView().tabItem { Label("Thư viện", systemImage: "folder") }.tag(1)
    SettingsView().tabItem { Label("Cài đặt", systemImage: "gearshape") }.tag(2)
}
.tint(Color.tealPrimary) // Selected color #2DD4BF
```

## ScanSession Equivalent (iOS)

Android dùng `ScanSession` singleton để truyền Bitmap giữa screens. iOS cần tương đương:

```swift
// ScanSession.swift
class ScanSession: ObservableObject {
    static let shared = ScanSession()
    var capturedImagePath: String?
    var sourceBitmap: UIImage?
    var corners: [CGPoint] = []
    var pendingImportPaths: [String] = []

    func clear() {
        sourceBitmap = nil
        capturedImagePath = nil
        corners = []
    }
}
```
