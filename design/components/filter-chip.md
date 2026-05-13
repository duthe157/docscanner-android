# FilterChip Component

> **Android**: Material3 `FilterChip` trong `EditScreen.kt` và `SettingsScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Chip để chọn một option trong nhóm (filter ảnh, export format, default filter). Chỉ một chip được selected tại một thời điểm trong nhóm.

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `selected` | `Boolean` | ✅ | — | Đang được chọn hay không |
| `onClick` | `() -> Unit` | ✅ | — | Tap để chọn |
| `label` | `String` | ✅ | — | Text hiển thị |
| `enabled` | `Boolean` | ❌ | `true` | Có thể tương tác không |
| `modifier` | `Modifier` | ❌ | `Modifier` | Android only |

## Measurements

| Element | Giá trị | Token |
|---|---|---|
| Height | 32dp | `component.filterChip.height` |
| Radius | full (pill) | `component.filterChip.radius` |
| Font | 13sp Medium | `typography.labelMedium` |
| Padding H | 12dp (Material3 default) | — |

## States

### unselected
- Bg: transparent / surface
- Border: outline color
- Text: onSurface

### selected
- Bg: `#1A3D3A` (primaryContainer)
- Border: primary `#2DD4BF`
- Text: `#2DD4BF` (onPrimaryContainer)
- Leading check icon (Material3 default)

### disabled
- Opacity reduced
- Not interactive

## Android Implementation
```kotlin
// EditScreen.kt
FilterChip(
    selected = uiState.filter == type,
    onClick = { viewModel.applyFilter(type) },
    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
    modifier = Modifier.weight(1f),
    enabled = !uiState.isSaving
)
```

## iOS Implementation
```swift
struct FilterChip: View {
    let label: String
    let isSelected: Bool
    let isEnabled: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: isEnabled ? onTap : {}) {
            Text(label)
                .font(AppFont.labelMedium)
                .foregroundColor(isSelected ? .tealPrimary : .textPrimary)
                .padding(.horizontal, 12)
                .frame(height: 32)
                .background(isSelected ? Color.tealContainer : Color.clear)
                .overlay(
                    Capsule().stroke(
                        isSelected ? Color.tealPrimary : Color.borderSubtle,
                        lineWidth: 1
                    )
                )
                .clipShape(Capsule())
                .opacity(isEnabled ? 1.0 : 0.4)
        }
        .disabled(!isEnabled)
    }
}
```
