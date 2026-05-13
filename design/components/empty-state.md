# EmptyState Component

> **Android**: Inline `EmptyLibraryState` trong `HomeScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Hiển thị trạng thái trống khi không có dữ liệu. Dùng ở HomeScreen và DocumentScreen.

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `onScanClick` | `() -> Unit` | ✅ | — | Tap nút action |
| `modifier` | `Modifier` | ❌ | `Modifier` | Android only |

## Layout

```
        ┌──────────────┐
        │  ○ icon 40dp │  ← Circle bg #1A3D3A, 80dp
        └──────────────┘
        
     "Chưa có tài liệu nào"     ← 16sp Medium
  "Nhấn Scan để bắt đầu..."     ← 12sp #8A9E9D, centered
  
        [  Scan ngay  ]          ← PrimaryButton, radius 12dp
```

## Measurements

| Element | Giá trị | Token |
|---|---|---|
| Icon circle size | 80dp | `icon.empty` |
| Icon circle bg | `#1A3D3A` | `color.action.container` |
| Icon circle shape | Circle | `radius.full` |
| Icon size | 40dp | `component.permissionView.iconSize` / 2 |
| Icon color | `#2DD4BF` | `color.action.primary` |
| Gap circle → title | 20dp | `spacing.7` |
| Title font | 16sp Medium | `typography.titleMedium` |
| Title color | `#FFFFFF` | `color.text.primary` |
| Gap title → subtitle | 8dp | `spacing.3` |
| Subtitle font | 12sp Regular | `typography.bodySmall` |
| Subtitle color | `#8A9E9D` | `color.text.secondary` |
| Subtitle align | center | — |
| Gap subtitle → button | 24dp | `spacing.8` |
| Button label | "Scan ngay" | — |
| Button icon | CameraAlt, 18dp | — |
| Button radius | 12dp | `radius.button` |
| Button bg | `#2DD4BF` | `color.action.primary` |
| Button text color | `#0D1F1E` | `color.text.onPrimary` |

## Variants

### Home variant (không có tài liệu)
- Icon: DocumentScanner
- Title: "Chưa có tài liệu nào"
- Subtitle: "Nhấn Scan để bắt đầu scan tài liệu đầu tiên"
- Button: "Scan ngay"

### Document variant (tài liệu trống)
- Icon: NoteAdd
- Title: "Tài liệu chưa có trang nào"
- Subtitle: "Nhấn nút bên dưới để thêm trang đầu tiên"
- Button: "Thêm trang"

## Android Implementation
```kotlin
@Composable
fun EmptyLibraryState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

## iOS Implementation
```swift
struct EmptyState: View {
    let icon: String          // SF Symbol name
    let title: String
    let subtitle: String
    let actionLabel: String
    let onAction: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color.tealContainer)
                    .frame(width: 80, height: 80)
                Image(systemName: icon)
                    .font(.system(size: 40))
                    .foregroundColor(.tealPrimary)
            }
            Spacer().frame(height: 20)
            Text(title).font(AppFont.titleMedium).foregroundColor(.textPrimary)
            Spacer().frame(height: 8)
            Text(subtitle).font(AppFont.bodySmall).foregroundColor(.textSecondary).multilineTextAlignment(.center)
            Spacer().frame(height: 24)
            Button(action: onAction) {
                Label(actionLabel, systemImage: "camera")
                    .font(AppFont.labelLarge)
                    .foregroundColor(.textOnPrimary)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color.tealPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }
}
```
