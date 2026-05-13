# Flow: Import Image to PDF

> **Status**: Implemented on Android ✅

## Goal
User import ảnh từ thư viện thiết bị, xử lý và xuất PDF.

## Entry Points
- HomeScreen → Quick Action "Import"
- HomeScreen → Library tab → FAB → (future: import option)

## Steps

```
1. HomeScreen
   └─ Tap Quick Action "Import"
   
2. System Image Picker
   ├─ User chọn 1 hoặc nhiều ảnh
   └─ Confirm selection
   
3. [Nếu nhiều ảnh] ScanSession.pendingImportPaths
   └─ Lưu các ảnh còn lại vào queue
   
4. DetectionScreen (ảnh đầu tiên)
   ├─ Auto-detect 4 góc
   ├─ [Optional] User điều chỉnh
   └─ Tap "Xác nhận"
   
5. EditScreen
   ├─ [Optional] Chỉnh filter/brightness/contrast
   └─ Tap "Lưu trang"
   
6. [Nếu còn ảnh trong queue]
   └─ Tự động navigate to DetectionScreen với ảnh tiếp theo
   
7. DocumentScreen
   └─ Tất cả trang đã được thêm vào document
   
8. ExportScreen → Share
```

## Multi-image Handling

Android implementation:
```kotlin
// HomeScreen.kt
ScanSession.pendingImportPaths = paths.drop(1).toMutableList()
onImportImage(paths.first())

// NavGraph.kt — sau khi lưu trang
val nextPath = ScanSession.pendingImportPaths.removeFirstOrNull()
if (nextPath != null) {
    navController.navigate(Screen.Detection.createRoute(nextPath, savedDocId)) {
        popUpTo(Screen.Document.createRoute(savedDocId))
    }
}
```

iOS cần implement tương tự với một queue mechanism.

## Permission Handling

### Storage/Photos
- Android 13+: `READ_MEDIA_IMAGES`
- Android < 13: `READ_EXTERNAL_STORAGE`
- iOS: `PHPhotoLibrary.requestAuthorization`
- Nếu denied: hiển thị `PermissionView`

## Offline Behavior
- Hoàn toàn offline

## Android / iOS Parity

| Element | Android | iOS |
|---|---|---|
| Image picker | `ActivityResultContracts.OpenMultipleDocuments()` | `PHPickerViewController` |
| Multi-select | Hỗ trợ | Hỗ trợ |
| File copy to cache | `contentResolver.openInputStream` | `PHAsset` → `UIImage` → save to temp |
| Queue mechanism | `ScanSession.pendingImportPaths` | Tương tự — cần implement |

## Acceptance Criteria
- [ ] Image picker mở khi tap Import
- [ ] Chọn 1 ảnh → flow bình thường
- [ ] Chọn nhiều ảnh → xử lý tuần tự từng ảnh
- [ ] Sau khi lưu trang cuối → DocumentScreen với tất cả trang
- [ ] Permission denied → PermissionView hiển thị
