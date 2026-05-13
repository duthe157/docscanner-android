# Document Detail Screen

> **Status**: Implemented on Android ✅
> **Source**: `presentation/document/DocumentScreen.kt`

## Purpose
Xem và quản lý các trang của một tài liệu. Hỗ trợ drag-to-reorder, edit, delete trang.

## Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│  TopAppBar                      │
│  [←] "Tên tài liệu"  [✏️][📤]  │
│       "N trang · dd/MM/yyyy"    │
├─────────────────────────────────┤
│                                 │
│  [PageItem]  ← drag handle      │
│  [PageItem]                     │
│  [PageItem]                     │
│  ...                            │
│                                 │
│              [+ Thêm trang FAB] │
├─────────────────────────────────┤
│  Navigation Bar                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| TopAppBar title font | 16sp Medium | `typography.titleMedium` |
| TopAppBar subtitle font | 12sp Regular | `typography.bodySmall` |
| TopAppBar subtitle color | onSurface opacity 60% | — |
| Rename icon | DriveFileRenameOutline, 24dp | — |
| Export icon | IosShare, 24dp | — |
| List padding H | 16dp | `spacing.screenPadding` |
| List padding top | 12dp | `spacing.5` |
| List padding bottom | 100dp | (FAB clearance) |
| Item gap | 10dp | `spacing.itemGap` |
| FAB label | "Thêm trang" | — |
| FAB icon | Add | — |
| FAB type | ExtendedFloatingActionButton | — |

### PageItem

| Element | Giá trị | Token |
|---|---|---|
| Card bg (idle) | `#1A2E2D` | `color.background.secondary` |
| Card bg (dragging) | `#1F3533` | `color.background.elevated` |
| Card radius | 12dp | `component.pageItem.radius` |
| Card elevation (idle) | 1dp | `shadow.card` |
| Card elevation (dragging) | 8dp | `shadow.cardDragging` |
| Card padding | 10dp | `component.pageItem.padding` |
| Drag handle size | 36dp × 36dp | `component.pageItem.dragHandleSize` |
| Drag handle icon | DragHandle, 20dp | — |
| Drag handle color | onSurface opacity 35% | — |
| Gap handle → thumbnail | 4dp | — |
| Thumbnail width | 56dp | `component.pageItem.thumbnailWidth` |
| Thumbnail height | 76dp | `component.pageItem.thumbnailHeight` |
| Thumbnail radius | 8dp | `component.pageItem.thumbnailRadius` |
| Thumbnail bg | `#1F3533` | `color.background.elevated` |
| Gap thumbnail → text | 12dp | — |
| Page title font | 14sp Medium | `typography.titleSmall` |
| Filter label font | 12sp Regular | `typography.bodySmall` |
| Filter label color | onSurface opacity 55% | — |
| Edit button size | 36dp × 36dp | `component.pageItem.actionButtonSize` |
| Edit button type | FilledTonalIconButton | — |
| Edit icon | Edit, 18dp | — |
| Delete button size | 36dp × 36dp | — |
| Delete icon | DeleteOutline, 20dp | — |
| Delete icon color | error opacity 70% | — |

## Filter Labels in PageItem

| FilterType | Label |
|---|---|
| ORIGINAL | "Gốc" |
| BW | "Đen trắng" |
| COLOR | "Màu sắc" |
| DARK | "Tối" |

## States

### loading
- `CircularProgressIndicator` ở giữa

### empty (không có trang)
- Icon NoteAdd, 72dp, primary opacity 30%
- Title: "Tài liệu chưa có trang nào"
- Subtitle: "Nhấn nút bên dưới để thêm trang đầu tiên"
- Button "Thêm trang" — primary, radius 12dp

### content
- Reorderable list với drag handles

### error
- Snackbar với message

## Dialogs

### Delete Page Confirm
- Icon: Delete, màu error
- Title: "Xóa trang?"
- Text: "Trang này sẽ bị xóa vĩnh viễn và không thể khôi phục."
- Confirm: Button "Xóa" bg error
- Cancel: OutlinedButton "Hủy"

### Rename Document Dialog
- Icon: DriveFileRenameOutline
- Title: "Đổi tên tài liệu"
- Input: OutlinedTextField label "Tên tài liệu"
- Confirm: Button "Lưu" (disabled nếu blank)
- Cancel: OutlinedButton "Hủy"

## Behavior

- **Drag handle** → drag-to-reorder (haptic feedback on reorder)
- **Tap edit button** → navigate to EditScreen với pageId
- **Tap delete button** → hiển thị delete confirm dialog
- **Tap rename icon** → hiển thị rename dialog
- **Tap export icon** → navigate to ExportScreen
- **Tap FAB** → navigate to CameraScreen với documentId

## Navigation

**Entry:** HomeScreen (tap DocumentCard), EditScreen (sau khi lưu)
**Exit:** → EditScreen (edit page), → CameraScreen (add page), → ExportScreen (export)
**Back:** → HomeScreen

## iOS Notes

- Drag-to-reorder: `List` với `.onMove` modifier hoặc custom drag gesture
- `ExtendedFloatingActionButton` → custom `Button` với label + icon
- `FilledTonalIconButton` → `Button` với `.bordered` + tonal color
- `AlertDialog` → `.alert` modifier hoặc custom sheet
- `OutlinedTextField` → `TextField` với `.roundedBorder` style
