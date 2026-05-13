# PageThumbnail / PageItem Component

> **Android**: Inline `PageItem` trong `DocumentScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Row item hiển thị một trang trong DocumentScreen. Có drag handle, thumbnail, info, edit/delete buttons.

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `page` | `Page` | ✅ | — | Dữ liệu trang |
| `index` | `Int` | ✅ | — | Số thứ tự (0-based) |
| `isDragging` | `Boolean` | ✅ | — | Đang được kéo |
| `onEdit` | `() -> Unit` | ✅ | — | Tap edit |
| `onDelete` | `() -> Unit` | ✅ | — | Tap delete |

## Layout

```
┌──────────────────────────────────────────────┐
│ [≡]  ┌──────┐  Trang N          [✏️] [🗑️]  │
│drag  │thumb │  Gốc / Đen trắng / ...         │
│36dp  │56×76 │                                │
│      └──────┘                                │
└──────────────────────────────────────────────┘
  padding: 10dp, card radius: 12dp
```

## Measurements

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
| Page title | "Trang {N+1}", 14sp Medium | `typography.titleSmall` |
| Filter label | 12sp, onSurface opacity 55% | `typography.bodySmall` |
| Edit button | FilledTonalIconButton, 36dp | — |
| Edit icon | Edit, 18dp | — |
| Delete button | IconButton, 36dp | — |
| Delete icon | DeleteOutline, 20dp, error opacity 70% | — |

## Filter Labels

| FilterType | Label |
|---|---|
| ORIGINAL | "Gốc" |
| BW | "Đen trắng" |
| COLOR | "Màu sắc" |
| DARK | "Tối" |

## States

### idle
- Card bg `#1A2E2D`, elevation 1dp

### dragging
- Card bg `#1F3533`, elevation 8dp
- Visual lift effect

## Android Implementation
```kotlin
@Composable
fun PageItem(
    page: Page,
    index: Int,
    isDragging: Boolean,
    scope: ReorderableCollectionItemScope,
    onEdit: () -> Unit,
    onDelete: () -> Unit
)
// Dùng với ReorderableItem từ sh.calvin.reorderable
```

## iOS Implementation
```swift
struct PageItem: View {
    let page: Page
    let index: Int
    let onEdit: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            // Drag handle (iOS List .onMove handles this automatically)
            Image(systemName: "line.3.horizontal")
                .font(.system(size: 20))
                .foregroundColor(.textPrimary.opacity(0.35))
                .frame(width: 36, height: 36)

            Spacer().frame(width: 4)

            // Thumbnail 56×76pt
            AsyncImage(url: URL(fileURLWithPath: page.previewPath)) { image in
                image.resizable().aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.backgroundElevated
            }
            .frame(width: 56, height: 76)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            Spacer().frame(width: 12)

            // Info
            VStack(alignment: .leading, spacing: 2) {
                Text("Trang \(index + 1)").font(AppFont.titleSmall)
                Text(filterLabel(page.filter)).font(AppFont.bodySmall).foregroundColor(.textSecondary)
            }
            Spacer()

            // Edit button
            Button(action: onEdit) {
                Image(systemName: "pencil").font(.system(size: 18))
            }
            .frame(width: 36, height: 36)
            .background(Color.tealContainer)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            Spacer().frame(width: 4)

            // Delete button
            Button(action: onDelete) {
                Image(systemName: "trash").font(.system(size: 20)).foregroundColor(.statusError.opacity(0.7))
            }
            .frame(width: 36, height: 36)
        }
        .padding(10)
        .background(Color.backgroundSecondary)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
```

**iOS drag-to-reorder**: Dùng `List` với `.onMove` modifier — iOS handle drag handle tự động.
