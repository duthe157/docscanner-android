# Settings Screen

> **Status**: Implemented on Android ✅
> **Source**: `presentation/settings/SettingsScreen.kt`

## Purpose
Cài đặt định dạng xuất mặc định, filter mặc định, xem dung lượng, xóa ảnh gốc.

## Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│  TopAppBar                      │
│  [←]  "Cài đặt"                │
├─────────────────────────────────┤
│  (scrollable, padding 16dp)     │
│                                 │
│  "Định dạng xuất mặc định"      │  ← section title (primary color)
│  [PDF] [JPG] [PNG]              │  ← FilterChips
│                                 │
│  ─────────────────────────────  │  ← Divider
│                                 │
│  "Bộ lọc mặc định khi scan"     │
│  [Gốc] [Đen trắng]             │
│  [Màu sắc] [Tối]               │
│                                 │
│  ─────────────────────────────  │
│                                 │
│  "Dung lượng đang dùng"         │
│  "125.3 MB"          [Làm mới]  │
│  [Xóa ảnh gốc (giữ ảnh đã xử lý)]│
│                                 │
│  Navigation Bar                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| Screen padding | 16dp | `spacing.screenPadding` |
| Section gap | 24dp | `spacing.sectionGap` |
| Section title font | 14sp Medium | `typography.titleSmall` |
| Section title color | `#2DD4BF` (primary) | `color.action.primary` |
| Section content gap | 12dp | `spacing.5` |
| FilterChip gap | 8dp | `spacing.3` |
| FilterChip height | 32dp | `component.filterChip.height` |
| FilterChip radius | full (pill) | `component.filterChip.radius` |
| Storage value font | 16sp Medium | `typography.titleMedium` |
| Storage value color | `#2DD4BF` (primary) | `color.action.primary` |
| "Làm mới" button | TextButton | — |
| Delete originals button | OutlinedButton, full width | — |
| Delete originals color | error `#FF5252` | `color.status.error` |

## Export Format Labels

| ExportFormat | Label |
|---|---|
| PDF | "PDF" |
| JPG | "JPG" |
| PNG | "PNG" |

## Filter Labels

| FilterType | Label |
|---|---|
| ORIGINAL | "Gốc" |
| BW | "Đen trắng" |
| COLOR | "Màu sắc" |
| DARK | "Tối" |

## Storage Display Format

```
< 1024 B    → "X B"
< 1 MB      → "X.X KB"
< 1 GB      → "X.X MB"
≥ 1 GB      → "X.XX GB"
```

## States

### content
- Tất cả settings hiển thị

### calculating storage
- `CircularProgressIndicator` 20dp thay cho storage value

## Dialogs

### Delete Originals Confirm
- Title: "Xóa ảnh gốc?"
- Text: "Ảnh gốc sẽ bị xóa để giải phóng dung lượng. Ảnh đã xử lý vẫn được giữ lại."
- Confirm: TextButton "Xóa" màu error
- Cancel: TextButton "Hủy"

## Behavior

- **Tap format chip** → `viewModel.setDefaultExportFormat()` → lưu vào SharedPreferences
- **Tap filter chip** → `viewModel.setDefaultFilter()` → lưu vào SharedPreferences
- **Tap "Làm mới"** → `viewModel.calculateStorage()` → tính lại dung lượng
- **Tap "Xóa ảnh gốc"** → hiển thị confirm dialog
- **Confirm xóa** → `viewModel.confirmDeleteOriginals()` → xóa files trong `originals/`
- **Back (←)** → về HomeScreen

## Navigation

**Entry:** HomeScreen (tab Cài đặt)
**Exit:** → HomeScreen (back)

## iOS Notes

- `SharedPreferences` → `UserDefaults`
- `FilterChip` → custom toggle button
- `OutlinedButton` với error color → `Button` với `.bordered` + red tint
- `Divider` → `Divider()` SwiftUI
- Storage calculation: `FileManager.default.attributesOfItem(atPath:)`
