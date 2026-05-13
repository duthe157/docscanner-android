# ScanCaptureButton Component

> **Android**: Inline trong `CameraScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Nút chụp ảnh (shutter button) trên CameraScreen. Có outer ring và inner button.

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `isCapturing` | `Boolean` | ✅ | — | Đang chụp hay không |
| `onClick` | `() -> Unit` | ✅ | — | Tap để chụp |
| `modifier` | `Modifier` | ❌ | `Modifier` | Android only |

## Layout

```
    ┌──────────────────────┐  ← Outer ring: 80dp circle, white 25%
    │   ┌──────────────┐   │
    │   │  ● or ⟳     │   │  ← Inner button: 64dp circle
    │   └──────────────┘   │
    └──────────────────────┘
```

## Measurements

| Element | Giá trị | Token |
|---|---|---|
| Outer size | 80dp | `component.scanButton.outerSize` |
| Outer bg | `Color.White` opacity 25% | — |
| Outer shape | Circle | `radius.full` |
| Inner size | 64dp | `component.scanButton.innerSize` |
| Inner bg (idle) | `Color.White` | — |
| Inner bg (capturing) | `Color.Gray` | — |
| Inner shape | Circle | `radius.full` |
| Icon (idle) | CameraAlt, 28dp, `Color.DarkGray` | `component.scanButton.iconSize` |
| Spinner (capturing) | 24dp, `Color.DarkGray`, stroke 2dp | — |

## States

### idle
- Inner bg: white
- Icon: CameraAlt, dark gray

### capturing
- Inner bg: gray
- Spinner: CircularProgressIndicator 24dp dark gray
- onClick disabled

## Android Implementation
```kotlin
// Inline trong CameraScreen.kt
Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)))
    FilledIconButton(
        onClick = { /* capture */ },
        modifier = Modifier.size(64.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isCapturing) Color.Gray else Color.White
        )
    ) {
        if (isCapturing) CircularProgressIndicator(...)
        else Icon(Icons.Default.CameraAlt, ...)
    }
}
```

## iOS Implementation
```swift
struct ScanCaptureButton: View {
    let isCapturing: Bool
    let onTap: () -> Void

    var body: some View {
        ZStack {
            Circle()
                .fill(Color.white.opacity(0.25))
                .frame(width: 80, height: 80)
            Button(action: isCapturing ? {} : onTap) {
                ZStack {
                    Circle()
                        .fill(isCapturing ? Color.gray : Color.white)
                        .frame(width: 64, height: 64)
                    if isCapturing {
                        ProgressView()
                            .progressViewStyle(.circular)
                            .tint(.darkGray)
                            .scaleEffect(0.8)
                    } else {
                        Image(systemName: "camera.fill")
                            .font(.system(size: 28))
                            .foregroundColor(.darkGray)
                    }
                }
            }
            .disabled(isCapturing)
        }
    }
}
```
