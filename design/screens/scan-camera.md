# Scan Camera Screen

> **Status**: Implemented on Android ✅
> **Source**: `presentation/camera/CameraScreen.kt`

## Purpose
Camera preview full screen để chụp ảnh tài liệu. Có guide frame A4, flash toggle, shutter button.

## Layout Baseline
- Full screen, edge-to-edge
- Background: `Color.Black` (không phải theme background)
- Orientation: Portrait only

## Structure
```
┌─────────────────────────────────┐
│  Camera Preview (full screen)   │
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │   Guide Frame (A4 ratio)  │  │
│  │   border: #2196F3 2dp     │  │
│  │   corners: white 3dp      │  │
│  │                           │  │
│  └───────────────────────────┘  │
│                                 │
│                    [⚡ Flash]   │ ← top right, status bar padding
│                                 │
│  "Đặt tài liệu vào khung..."   │ ← bottom, 13sp white 80%
│         [● Shutter]             │ ← 80dp outer, 64dp inner
│                                 │
└─────────────────────────────────┘
```

## Exact Measurements

| Element | Giá trị | Token |
|---|---|---|
| Background | `Color.Black` | — |
| Guide frame width | 85% screen width | — |
| Guide frame height | width × 1.414 (A4 ratio) | — |
| Guide frame offset Y | center - 4% screen height | — |
| Guide frame radius | 12dp | `radius.md` |
| Guide frame border color | `#2196F3` | `color.camera.guideBorder` |
| Guide frame border width | 2dp | — |
| Guide scrim | `Color.Black` opacity 45% | — |
| Corner accent length | 24dp | — |
| Corner accent color | `Color.White` | `color.camera.guideCorner` |
| Corner accent stroke | 3dp | — |
| Flash button size | 44dp × 44dp | — |
| Flash button bg | `Color.Black` opacity 40% | — |
| Flash button shape | Circle | — |
| Flash button padding top | statusBarInset + 8dp | — |
| Flash button padding H | 16dp | `spacing.screenPadding` |
| Flash icon size | 24dp | `icon.xl` |
| Flash ON icon color | `Color.Yellow` | — |
| Flash OFF icon color | `Color.White` | — |
| Hint text font | 13sp Regular | — |
| Hint text color | `Color.White` opacity 80% | — |
| Shutter outer size | 80dp | `component.scanButton.outerSize` |
| Shutter outer bg | `Color.White` opacity 25% | — |
| Shutter outer shape | Circle | — |
| Shutter inner size | 64dp | `component.scanButton.innerSize` |
| Shutter inner bg (idle) | `Color.White` | — |
| Shutter inner bg (capturing) | `Color.Gray` | — |
| Shutter icon size | 28dp | `component.scanButton.iconSize` |
| Shutter icon color | `Color.DarkGray` | — |
| Bottom controls padding bottom | 40dp + navBarInset | — |
| Bottom controls gap | 16dp | `spacing.6` |

## Permission Denied State

| Element | Giá trị |
|---|---|
| Background | `#0D1F1E` (theme background) |
| Icon | CameraAlt, 72dp, `#2DD4BF` opacity 50% |
| Title | "Cần quyền truy cập camera" — 18sp SemiBold |
| Body (rationale) | "App cần quyền camera để chụp ảnh tài liệu. Vui lòng cấp quyền để tiếp tục." |
| Body (denied) | "Quyền camera đã bị từ chối. Vào Cài đặt để cấp quyền thủ công." |
| Button (rationale) | "Cấp quyền camera" — full width, radius 12dp, primary teal |
| Button (denied) | "Mở Cài đặt" — full width, radius 12dp, primary teal |
| Padding H | 32dp |

## States

### idle — camera ready
- Preview live, guide frame visible, shutter white

### capturing
- Shutter inner bg → `Color.Gray`
- `CircularProgressIndicator` 24dp `Color.DarkGray` inside shutter
- Brief flash animation (white overlay)

### error
- `Snackbar` ở bottom với message + "OK" button

### permission_denied
- Xem Permission Denied State ở trên

## Behavior

- **Tap preview** → tap-to-focus tại điểm đó
- **Tap flash** → toggle flash ON/OFF, icon thay đổi
- **Tap shutter** → chụp ảnh → navigate to DetectionScreen
- **Back** → về HomeScreen

## Navigation

**Entry:** HomeScreen (Scan), DocumentScreen (Thêm trang)
**Exit:** → DetectionScreen (sau khi chụp)
**Back:** → HomeScreen hoặc DocumentScreen

## iOS Notes

- Camera preview: `AVCaptureVideoPreviewLayer` trong `UIViewRepresentable`
- Flash: `AVCaptureDevice.torchMode` hoặc `AVCaptureDevice.flashMode`
- Guide frame: `Canvas` / `Path` overlay trên preview
- Permission: `AVCaptureDevice.requestAccess(for: .video)`
- Shutter: custom `Button` với Circle shape
- `statusBarsPadding` → `.safeAreaInset(edge: .top)`
- `navigationBarsPadding` → `.safeAreaInset(edge: .bottom)`
