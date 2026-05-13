# DocumentCard Component

> **Android**: Inline `DocumentItem` trong `HomeScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Card hiển thị một tài liệu trong danh sách. Dùng ở HomeScreen (cả Home tab và Library tab).

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `document` | `Document` | ✅ | — | Dữ liệu tài liệu |
| `onClick` | `() -> Unit` | ✅ | — | Tap vào card |
| `onDelete` | `() -> Unit` | ✅ | — | Chọn Xóa từ menu |
| `onMerge` | `() -> Unit` | ❌ | `{}` | Chọn Hợp nhất từ menu |
| `modifier` | `Modifier` | ❌ | `Modifier` | Android only |

## Layout

```
┌─────────────────────────────────────────┐  ← Card bg #1A2E2D, radius 12dp, border 1dp #243534
│  ┌──────┐  Tên tài liệu (14sp Medium)   [⋮]│
│  │thumb │  📄 N trang  (11sp #8A9E9D)       │
│  │52×70 │  dd/MM/yyyy · HH:mm (11sp)        │
│  └──────┘                                   │
└─────────────────────────────────────────┘
  padding: 12dp all sides
  thumbnail radius: 8dp
  gap thumb→text: 14dp
```

## Measurements

| Element | Giá trị | Token |
|---|---|---|
| Card bg | `#1A2E2D` | `color.background.secondary` |
| Card radius | 12dp | `radius.card` |
| Card border | 1dp `#243534` | `color.border.subtle` |
| Card padding | 12dp | `component.documentCard.padding` |
| Thumbnail width | 52dp | `component.documentCard.thumbnailWidth` |
| Thumbnail height | 70dp | `component.documentCard.thumbnailHeight` |
| Thumbnail radius | 8dp | `component.documentCard.thumbnailRadius` |
| Thumbnail bg | `#1F3533` | `color.background.elevated` |
| Thumbnail placeholder icon | 26dp, `#8A9E9D` opacity 40% | — |
| Gap thumbnail → text | 14dp | — |
| Document name | 14sp Medium, `#FFFFFF` | `typography.titleSmall` |
| Page count icon | 12dp, `#8A9E9D` | — |
| Page count text | 11sp, `#8A9E9D` | `typography.labelSmall` |
| Date text | 11sp, `#8A9E9D` | `typography.labelSmall` |
| More icon | 24dp, `#8A9E9D` | `icon.xl` |

## Date Format
`dd/MM/yyyy · HH:mm` (ví dụ: `26/02/2026 · 14:30`)

## More Menu Items
1. "Hợp nhất tài liệu" — icon MergeType
2. "Xóa" — icon DeleteOutline, text màu error `#FF5252`

## States

### default
- Card bg `#1A2E2D`, border `#243534`

### pressed
- Ripple effect trên toàn card

### thumbnail loaded
- AsyncImage hiển thị preview ảnh

### thumbnail missing
- Icon Description placeholder, opacity 40%

## Accessibility
- Card: role = button, content description = "{tên tài liệu}, {N} trang, {ngày}"
- More button: content description = "Tùy chọn cho {tên tài liệu}"
- Touch target: toàn card (min 48dp height)

## Android Implementation
```kotlin
@Composable
fun DocumentItem(
    document: Document,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMerge: () -> Unit = {},
    modifier: Modifier = Modifier
)
```
Hiện tại là private composable trong `HomeScreen.kt`. Có thể extract ra `presentation/components/DocumentCard.kt`.

## iOS Implementation
```swift
struct DocumentCard: View {
    let document: Document
    let onTap: () -> Void
    let onDelete: () -> Void
    let onMerge: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            // Thumbnail 52×70pt, radius 8pt
            // Text column
            // Menu button
        }
        .padding(12)
        .background(Color.backgroundSecondary)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.borderSubtle, lineWidth: 1))
        .onTapGesture(perform: onTap)
    }
}
```
