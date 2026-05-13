# Crop Document Screen (Detection Screen)

> **Status**: Implemented on Android ✅
> **Source**: `presentation/detection/DetectionScreen.kt`

## Purpose
Hiển thị ảnh vừa chụp với 4 drag handles để user điều chỉnh vùng tài liệu trước khi crop.

## Layout Baseline
- Full screen với bottom bar
- Background: theme background `#0D1F1E`

## Structure
```
┌─────────────────────────────────┐
│  Status Bar                     │
├─────────────────────────────────┤
│                                 │
│  Image (ContentScale.Fit)       │
│  + 4 drag handles overlay       │
│  + selection polygon overlay    │
│  + corner zoom (top-right)      │
│                                 │
├─────────────────────────────────┤
│  Bottom Bar (4 buttons)         │
│  [Chụp lại][Reset góc][Detect lại][Xác nhận]│
│  Navigation Bar                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| Image content scale | Fit (letterbox) | — |
| Selection polygon fill | `#3300AAFF` (20% blue) | — |
| Selection polygon stroke | `#FF00AAFF` 2.5dp | — |
| Handle outer circle radius | 28dp | — |
| Handle outer circle color | `#5500AAFF` (33% blue) | — |
| Handle inner circle radius | 16dp | — |
| Handle inner color (idle) | `Color.White` | — |
| Handle inner color (active) | `#FFFFD700` (gold) | — |
| Handle dot radius | 11dp | — |
| Handle dot color (idle) | `#FF0088FF` (blue) | — |
| Handle dot color (active) | `#FFFF8C00` (orange) | — |
| Touch radius for drag | 60dp | — |
| Corner zoom size | 120dp × 120dp | `component.cornerZoomOverlay.size` |
| Corner zoom radius | 12dp | `component.cornerZoomOverlay.radius` |
| Corner zoom position | top-end, padding 12dp | — |
| Corner zoom bg | `Color.Black` opacity 70% | — |
| Crosshair length | 12dp | `component.cornerZoomOverlay.crosshairLength` |
| Crosshair stroke | 1.5dp | `component.cornerZoomOverlay.crosshairStroke` |
| Crosshair color | `Color.Red` | — |
| Bottom bar padding H | 12dp | — |
| Bottom bar padding V | 8dp | — |
| Bottom bar button gap | 8dp | `spacing.3` |
| "Chụp lại" button | OutlinedButton, weight 1f | — |
| "Reset góc" button | OutlinedButton, weight 1f | — |
| "Detect lại" button | OutlinedButton, weight 1f | — |
| "Xác nhận" button | FilledButton, weight 1.2f | — |
| Button font | 13sp Medium | `typography.labelMedium` |

## States

### loading
- `CircularProgressIndicator` ở giữa màn hình
- Text "Đang phát hiện tài liệu..." padding top 60dp
- "Xác nhận" button disabled

### success
- Ảnh hiển thị với 4 drag handles
- "Xác nhận" button enabled

### error
- Text "Không thể phát hiện tài liệu" màu error
- Button "Thử lại"

## Behavior

- **Drag handle** → kéo để di chuyển góc, real-time update
- **Long press handle** → auto-zoom (corner zoom overlay hiện ở top-right)
- **"Chụp lại"** → `onRetake()` → về CameraScreen
- **"Reset góc"** → khôi phục về corners auto-detect ban đầu
- **"Detect lại"** → chạy lại detection
- **"Xác nhận"** → `onConfirm(bitmap, corners)` → navigate to EditScreen

## Navigation

**Entry:** CameraScreen (sau khi chụp hoặc import)
**Exit:** → EditScreen (Xác nhận), → CameraScreen (Chụp lại)
**Back:** → CameraScreen

## iOS Notes

- Drag gesture: `DragGesture` trong SwiftUI
- Canvas overlay: `Canvas` view hoặc `Path` trong `ZStack`
- Corner zoom: crop `UIImage` / `CGImage` và display trong overlay
- Image display: `Image(uiImage:).resizable().aspectRatio(contentMode: .fit)`
