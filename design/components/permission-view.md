# PermissionView Component

> **Android**: Inline `PermissionDeniedScreen` trong `CameraScreen.kt` ✅
> **iOS**: Cần implement theo spec này

## Purpose
Hiển thị khi app thiếu quyền (camera, storage). Hướng dẫn user cấp quyền.

## Props / Data Contract

| Prop | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `shouldShowRationale` | `Boolean` | ✅ | — | Có thể hỏi lại hay không |
| `onRequestPermission` | `() -> Unit` | ✅ | — | Tap "Cấp quyền" |
| `onOpenSettings` | `() -> Unit` | ✅ | — | Tap "Mở Cài đặt" |
| `modifier` | `Modifier` | ❌ | `Modifier` | Android only |

## Layout

```
        [  📷 icon 72dp  ]   ← primary opacity 50%
        
   "Cần quyền truy cập camera"  ← 18sp SemiBold, centered
   
   "App cần quyền camera..."    ← 14sp, centered, opacity 70%
   
   [  Cấp quyền camera  ]       ← hoặc [  Mở Cài đặt  ]
```

## Measurements

| Element | Giá trị | Token |
|---|---|---|
| Background | `#0D1F1E` | `color.background.primary` |
| Icon size | 72dp | `component.permissionView.iconSize` |
| Icon color | `#2DD4BF` opacity 50% | `color.action.primary` |
| Gap icon → title | 16dp | `spacing.6` |
| Title font | 18sp SemiBold | `typography.titleLarge` |
| Title align | center | — |
| Gap title → body | 8dp | `spacing.3` |
| Body font | 14sp Regular | `typography.bodyMedium` |
| Body color | onSurface opacity 70% | — |
| Body align | center | — |
| Gap body → button | 24dp | `spacing.8` |
| Button width | full width | — |
| Button radius | 12dp | `radius.button` |
| Padding H | 32dp | `component.permissionView.paddingHorizontal` |

## Variants

### Camera — rationale (có thể hỏi lại)
- Icon: CameraAlt
- Title: "Cần quyền truy cập camera"
- Body: "App cần quyền camera để chụp ảnh tài liệu. Vui lòng cấp quyền để tiếp tục."
- Button: "Cấp quyền camera" → `onRequestPermission()`

### Camera — denied (đã từ chối vĩnh viễn)
- Icon: CameraAlt
- Title: "Cần quyền truy cập camera"
- Body: "Quyền camera đã bị từ chối. Vào Cài đặt để cấp quyền thủ công."
- Button: "Mở Cài đặt" → `onOpenSettings()`

## Android Implementation
```kotlin
@Composable
fun PermissionDeniedScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
)
```

## iOS Implementation
```swift
struct PermissionView: View {
    let icon: String
    let title: String
    let body: String
    let buttonLabel: String
    let onButtonTap: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Image(systemName: icon)
                .font(.system(size: 72))
                .foregroundColor(.tealPrimary.opacity(0.5))
            Spacer().frame(height: 16)
            Text(title).font(AppFont.titleLarge).multilineTextAlignment(.center)
            Spacer().frame(height: 8)
            Text(body).font(AppFont.bodyMedium).foregroundColor(.textPrimary.opacity(0.7)).multilineTextAlignment(.center)
            Spacer().frame(height: 24)
            Button(action: onButtonTap) {
                Text(buttonLabel)
                    .font(AppFont.labelLarge)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.tealPrimary)
                    .foregroundColor(.textOnPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .padding(.horizontal, 32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.backgroundPrimary)
    }
}
```

**iOS permission request:**
```swift
// Camera
AVCaptureDevice.requestAccess(for: .video) { granted in ... }

// Photos
PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in ... }
```
