# Export & Share Screen

> **Status**: Implemented on Android ✅
> **Source**: `presentation/export/ExportScreen.kt`

## Purpose
Chọn định dạng xuất (PDF/JPG/PNG), xem preview các trang, và thực hiện export/share/save.

## Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│  TopAppBar                      │
│  [←]  "Xuất tài liệu"          │
│        "Tên tài liệu"           │
├─────────────────────────────────┤
│  (scrollable)                   │
│  "N trang"                      │
│  ← [thumb][thumb][thumb] →      │  ← LazyRow thumbnails
│  ─────────────────────────────  │  ← Divider
│  "Định dạng xuất"               │
│  [PDF card] [JPG card] [PNG card]│
│                                 │
│  [Lưu vào thiết bị]             │  ← FilledButton
│  [Lưu vào Thư viện ảnh]         │  ← OutlinedButton
│  [Chia sẻ]                      │  ← OutlinedButton
│  [Về trang chủ]                 │  ← TextButton
│                                 │
│  Navigation Bar                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| TopAppBar title font | 16sp Medium | `typography.titleMedium` |
| TopAppBar subtitle font | 12sp Regular | `typography.bodySmall` |
| TopAppBar subtitle color | onSurface opacity 60% | — |
| Page count label | 13sp Medium, onSurface opacity 60% | `typography.labelMedium` |
| Page count padding H | 16dp | `spacing.screenPadding` |
| Page count padding V | 8dp | `spacing.3` |
| Thumbnail row padding H | 16dp | `spacing.screenPadding` |
| Thumbnail gap | 8dp | `spacing.3` |
| Thumbnail size | 80dp × 110dp | — |
| Thumbnail radius | 8dp | `radius.thumbnail` |
| Thumbnail bg | `#1F3533` | `color.background.elevated` |
| Divider padding H | 16dp | `spacing.screenPadding` |
| Section title font | 14sp SemiBold | `typography.titleSmall` |
| Section title padding H | 16dp | `spacing.screenPadding` |
| Format cards row padding H | 16dp | `spacing.screenPadding` |
| Format card gap | 10dp | `spacing.4` |
| Format card width | 90dp | `component.formatCard.width` |
| Format card radius | 12dp | `component.formatCard.radius` |
| Format card padding V | 12dp | `component.formatCard.paddingVertical` |
| Format card padding H | 8dp | `component.formatCard.paddingHorizontal` |
| Format card bg (selected) | `primaryContainer` = `#1A3D3A` | `color.action.container` |
| Format card bg (unselected) | `#1A2E2D` | `color.background.secondary` |
| Format card border (selected) | 2dp primary `#2DD4BF` | `color.action.primary` |
| Format card border (unselected) | 1dp outline opacity 30% | — |
| Format icon size | 28dp | `component.formatCard.iconSize` |
| Format icon color (selected) | `#2DD4BF` | `color.action.primary` |
| Format icon color (unselected) | onSurface opacity 60% | — |
| Format label font (selected) | 13sp Bold | `typography.labelMedium` |
| Format label font (unselected) | 13sp Medium | `typography.labelMedium` |
| Format desc font | 11sp Regular | `typography.labelSmall` |
| Action buttons padding H | 16dp | `spacing.screenPadding` |
| Action button gap | 10dp | `spacing.4` |
| Action button height | 52dp | `component.button.height` |
| Action button radius | 12dp | `component.button.radius` |
| Action button font | 16sp Regular | `typography.bodyLarge` |
| Action icon size | 20dp | `component.button.iconSize` |
| Bottom spacing | 24dp | `spacing.8` |

## Format Cards

| Format | Icon | Description |
|---|---|---|
| PDF | PictureAsPdf | "Đa trang" |
| JPG | Image | "Ảnh nén" |
| PNG | Image | "Ảnh gốc" |

## Action Buttons

| Button | Type | Label |
|---|---|---|
| Lưu vào thiết bị | FilledButton | Icon: SaveAlt |
| Lưu vào Thư viện ảnh | OutlinedButton | Icon: Photo |
| Chia sẻ | OutlinedButton | Icon: Share |
| Về trang chủ | TextButton | (no icon) |

## States

### loading
- `CircularProgressIndicator` ở giữa

### content
- Thumbnails + format selector + action buttons

### exporting (multi-page PDF)
- `LinearProgressIndicator` full width
- Text "Đang xuất X/N trang..."

### exporting (single / image)
- `CircularProgressIndicator`
- Text "Đang xuất..."

## Behavior

- **Tap format card** → select format, update card visual
- **Lưu vào thiết bị** → export file → save via MediaStore/SAF
- **Lưu vào Thư viện ảnh** → export image → save to Photos
- **Chia sẻ** → export file → open system Share Sheet
- **Về trang chủ** → `onDone()` → về HomeScreen
- **Back (←)** → `onDone()` → về HomeScreen/DocumentScreen

## Navigation

**Entry:** DocumentScreen (export icon), HomeScreen (share action)
**Exit:** → System Share Sheet, → HomeScreen (Về trang chủ / back)

## iOS Notes

- Share Sheet: `UIActivityViewController` wrapped in `UIViewControllerRepresentable`
- Save to Photos: `PHPhotoLibrary.shared().performChanges`
- Save to Files: `UIDocumentPickerViewController`
- `LinearProgressIndicator` → `ProgressView(value:)` với `.progressViewStyle(.linear)`
- `Divider` → `Divider()` SwiftUI
