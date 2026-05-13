# Home Screen

> **Status**: Implemented on Android ✅
> **Source**: `presentation/home/HomeScreen.kt`

## Figma Reference
```
Figma frame: TODO
Figma page:  TODO
Last synced: TODO
```

## Screenshot Path
```
Android: design/screenshots/android/screens/home-content.png
iOS:     design/screenshots/ios/screens/home-content.png
```

## Purpose
Màn hình chính. Có 2 tab: **Trang chủ** (dashboard) và **Thư viện** (full list). Tab Cài đặt navigate sang SettingsScreen.

## Layout Baseline
- Reference: 412×915dp (Pixel 7)
- Safe area: edge-to-edge, handle status bar + navigation bar
- Orientation: Portrait only
- Theme: Dark only

---

## Tab 1 — Trang chủ (Home Tab)

### Structure
```
┌─────────────────────────────────┐
│  Status Bar (transparent)       │
├─────────────────────────────────┤
│                                 │
│  "CamScanner"  (centered, 22sp) │
│                                 │
├─────────────────────────────────┤
│  "Thao tác nhanh"               │
│  [Import] [Scan] [Dán] [Thư viện]│
├─────────────────────────────────┤
│  "Tài liệu gần đây"  [Xem tất cả]│
│  ← [Card] [Card] [Card] [Card] →│
│     (horizontal scroll)         │
├─────────────────────────────────┤
│  "Tổng quan"                    │
│  ┌──────────────────────────┐   │
│  │  📄 N  │  📃 N           │   │
│  │ Tài liệu│  Trang         │   │
│  └──────────────────────────┘   │
├─────────────────────────────────┤
│  "Tất cả tài liệu"              │
│  [DocumentCard]                 │
│  [DocumentCard]                 │
│  ...                            │
├─────────────────────────────────┤
│  Bottom Nav                     │
│  [🏠 Trang chủ] [📁 Thư viện] [⚙️ Cài đặt]│
│  Navigation Bar                 │
└─────────────────────────────────┘
```

### Exact Measurements — Home Tab

| Element | Giá trị | Token |
|---|---|---|
| Screen background | `#0D1F1E` | `color.background.primary` |
| Title "CamScanner" | centered, 22sp SemiBold | `typography.headlineMedium` |
| Title padding top | 24dp | `spacing.8` |
| Title padding bottom | 8dp | `spacing.3` |
| Section header font | 14sp Medium | `typography.titleSmall` |
| Section header padding H | 16dp | `spacing.screenPadding` |
| Section header padding V | 12dp | `spacing.5` |
| Quick Actions row padding H | 16dp | `spacing.screenPadding` |
| Quick Action icon box size | 56dp × 56dp | `component.quickActionButton.iconBoxSize` |
| Quick Action icon box radius | 12dp | `component.quickActionButton.iconBoxRadius` |
| Quick Action icon box bg | `#1A3D3A` | `color.action.container` |
| Quick Action icon size | 24dp | `component.quickActionButton.iconSize` |
| Quick Action icon color | `#2DD4BF` | `color.action.primary` |
| Quick Action label font | 11sp Regular | `typography.labelSmall` |
| Quick Action label color | `#8A9E9D` | `color.text.secondary` |
| Quick Action label gap | 6dp | `component.quickActionButton.labelGap` |
| Quick Action padding H | 8dp | `component.quickActionButton.paddingHorizontal` |
| Quick Action padding V | 4dp | `component.quickActionButton.paddingVertical` |
| Recent docs LazyRow padding H | 16dp | `spacing.screenPadding` |
| Recent docs card gap | 12dp | `spacing.5` |
| Recent card width | 100dp | `component.recentDocumentCard.width` |
| Recent card thumbnail height | 80dp | `component.recentDocumentCard.thumbnailHeight` |
| Recent card padding | 8dp | `component.recentDocumentCard.padding` |
| Recent card radius | 12dp | `component.recentDocumentCard.radius` |
| Recent card border | 1dp `#243534` | `color.border.subtle` |
| Recent card bg | `#1A2E2D` | `color.background.secondary` |
| Storage card padding | 16dp | `component.storageOverviewCard.padding` |
| Storage card radius | 12dp | `component.storageOverviewCard.radius` |
| Storage card border | 1dp `#243534` | `color.border.subtle` |
| Storage card bg | `#1A2E2D` | `color.background.secondary` |
| Storage divider width | 1dp | `component.storageOverviewCard.dividerWidth` |
| Storage divider height | 48dp | `component.storageOverviewCard.dividerHeight` |
| Storage icon size | 24dp | `component.storageOverviewCard.iconSize` |
| Storage value font | 16sp Medium | `typography.titleMedium` |
| Storage label font | 11sp Regular | `typography.labelSmall` |
| Document list padding H | 16dp | `spacing.screenPadding` |
| Document list item gap | 4dp vertical | — |
| LazyColumn bottom padding | 24dp | `spacing.8` |

---

## Tab 2 — Thư viện (Library Tab)

### Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│  TopAppBar                      │
│  "Thư viện"              [🔍]   │
├─────────────────────────────────┤
│                                 │
│  [DocumentCard]                 │
│  [DocumentCard]                 │
│  ...                            │
│                                 │
│                        [+ FAB]  │
├─────────────────────────────────┤
│  Bottom Nav                     │
└─────────────────────────────────┘
```

### Exact Measurements — Library Tab

| Element | Giá trị | Token |
|---|---|---|
| TopAppBar bg | `#0D1F1E` | `color.background.primary` |
| TopAppBar title | 18sp SemiBold | `typography.titleLarge` |
| Search icon | 24dp, `#8A9E9D` | `color.icon.default` |
| List padding H | 16dp | `spacing.screenPadding` |
| List padding top | 8dp | `spacing.3` |
| List padding bottom | 88dp | (FAB clearance) |
| Item gap | 10dp | `spacing.itemGap` |
| FAB size | 56dp | `component.fab.size` |
| FAB radius | 12dp | `component.fab.radius` |
| FAB bg | `#2DD4BF` | `color.action.primary` |
| FAB icon color | `#0D1F1E` | `color.text.onPrimary` |
| FAB icon size | 24dp | `component.fab.iconSize` |

---

## DocumentCard (dùng ở cả 2 tab)

| Element | Giá trị | Token |
|---|---|---|
| Card bg | `#1A2E2D` | `color.background.secondary` |
| Card radius | 12dp | `component.documentCard.radius` |
| Card border | 1dp `#243534` | `color.border.subtle` |
| Card padding | 12dp | `component.documentCard.padding` |
| Thumbnail width | 52dp | `component.documentCard.thumbnailWidth` |
| Thumbnail height | 70dp | `component.documentCard.thumbnailHeight` |
| Thumbnail radius | 8dp | `component.documentCard.thumbnailRadius` |
| Thumbnail bg | `#1F3533` | `color.background.elevated` |
| Thumbnail placeholder icon | 26dp, `#8A9E9D` opacity 40% | — |
| Gap thumbnail → text | 14dp | — |
| Document name font | 14sp Medium | `typography.titleSmall` |
| Document name color | `#FFFFFF` | `color.text.primary` |
| Page count icon | 12dp, `#8A9E9D` | `color.icon.default` |
| Page count font | 11sp Regular | `typography.labelSmall` |
| Date font | 11sp Regular | `typography.labelSmall` |
| Date color | `#8A9E9D` | `color.text.secondary` |
| More icon | 24dp, `#8A9E9D` | `color.icon.default` |

---

## Bottom Navigation

| Element | Giá trị | Token |
|---|---|---|
| Container bg | `#111E1D` | `color.background.bottomNav` |
| Tonal elevation | 0dp | — |
| Selected icon color | `#2DD4BF` | `color.action.primary` |
| Selected text color | `#2DD4BF` | `color.action.primary` |
| Unselected icon color | `#8A9E9D` | `color.icon.default` |
| Unselected text color | `#8A9E9D` | `color.text.secondary` |
| Indicator bg | `#1A3D3A` | `color.action.container` |
| Tab 0 | Home icon (filled when selected) | — |
| Tab 1 | Folder icon (filled when selected) | — |
| Tab 2 | Settings icon → navigate to SettingsScreen | — |

---

## States

### loading
- `CircularProgressIndicator` màu `#2DD4BF` ở giữa màn hình
- Bottom nav vẫn hiển thị

### empty (không có tài liệu)
- Icon tròn 80dp bg `#1A3D3A`, icon `DocumentScanner` 40dp màu `#2DD4BF`
- Title: "Chưa có tài liệu nào" — 16sp Medium
- Subtitle: "Nhấn Scan để bắt đầu scan tài liệu đầu tiên" — 12sp Regular `#8A9E9D`
- Button "Scan ngay" — primary teal, radius 12dp

### content
- Home tab: title + quick actions + recent docs + storage + full list
- Library tab: full list + FAB

---

## Behavior

### Quick Actions
- **Import** → mở system image picker (multi-select), sau đó navigate to DetectionScreen
- **Scan** → navigate to CameraScreen
- **Dán** → placeholder (chưa implement)
- **Thư viện** → switch sang Library tab (selectedTab = 1)

### Recent Documents
- Lấy 4 tài liệu đầu từ danh sách (đã sort theo updatedAt giảm dần)
- Tap card → navigate to DocumentScreen
- "Xem tất cả" → switch sang Library tab

### Storage Overview
- Hiển thị tổng số tài liệu và tổng số trang
- Tính từ `uiState.documents`

### DocumentCard more menu
- **Hợp nhất tài liệu** → hiển thị AlertDialog chọn target
- **Xóa** → hiển thị AlertDialog confirm, màu error

### Tab Settings
- Tap → navigate to SettingsScreen (không switch tab)

---

## Navigation

**Entry points:**
- App launch (start destination)
- Back từ DocumentScreen, CameraScreen, SettingsScreen

**Exit points:**
- → CameraScreen (Quick Action Scan)
- → DetectionScreen (Quick Action Import, sau khi chọn ảnh)
- → DocumentScreen (tap DocumentCard)
- → SettingsScreen (tap tab Cài đặt)

**Back behavior:**
- Back → exit app

---

## Dialogs

### Delete Confirm Dialog
- Container bg: `#1F3533`
- Icon: Delete, màu error `#FF5252`
- Title: "Xóa tài liệu?"
- Text: "Tài liệu và tất cả trang sẽ bị xóa vĩnh viễn."
- Confirm: Button "Xóa" bg `#FF5252`
- Cancel: OutlinedButton "Hủy"

### Merge Target Picker Dialog
- Container bg: `#1F3533`
- Icon: MergeType, màu primary `#2DD4BF`
- Title: "Hợp nhất vào tài liệu nào?"
- Body: danh sách OutlinedButton cho mỗi target document
- Cancel: TextButton "Hủy"

---

## iOS Implementation Notes

- Bottom nav: `TabView` với `.tabViewStyle(.automatic)` hoặc custom `HStack` bottom bar
- `TabView` indicator color: set via `.tint(Color.tealPrimary)`
- Bottom nav bg: `Color.backgroundBottomNav` — set via `UITabBar.appearance().backgroundColor`
- LazyColumn → `LazyVStack` trong `ScrollView` hoặc `List`
- LazyRow → `ScrollView(.horizontal)` + `HStack`
- `NavigationBar` → `NavigationView` / `NavigationStack`
- FAB → `ZStack` với `Button` overlay ở bottom-right
- `Card` với border → `RoundedRectangle` stroke + fill
- `AsyncImage` (Coil) → `AsyncImage` (SwiftUI native, iOS 15+)
- `DropdownMenu` → `Menu` button hoặc `confirmationDialog`

---

## Acceptance Criteria

- [ ] Dark background `#0D1F1E` trên toàn màn hình
- [ ] Bottom nav bg `#111E1D`, selected color `#2DD4BF`, indicator `#1A3D3A`
- [ ] Home tab: title căn giữa, 4 quick action buttons đều nhau
- [ ] Quick action icon box 56dp, bg `#1A3D3A`, icon `#2DD4BF`
- [ ] Recent docs horizontal scroll, card width 100dp
- [ ] Storage card hiển thị đúng số tài liệu và trang
- [ ] Library tab: TopAppBar "Thư viện" + search icon + FAB teal
- [ ] DocumentCard: thumbnail 52×70dp, radius 8dp, border 1dp `#243534`
- [ ] FAB: bg `#2DD4BF`, icon `#0D1F1E`, radius 12dp
- [ ] Empty state: icon tròn teal, button "Scan ngay"
- [ ] Delete dialog: confirm button màu `#FF5252`
