# Flow: Scan to PDF

> **Status**: Implemented on Android ✅

## Goal
User chụp ảnh tài liệu bằng camera, crop, chỉnh sửa, lưu và xuất PDF.

## Entry Points
- HomeScreen → Quick Action "Scan"
- HomeScreen → Library tab → FAB (+)

## Steps

```
1. HomeScreen
   └─ Tap "Scan" hoặc FAB
   
2. CameraScreen
   ├─ Camera preview hiển thị với guide frame
   ├─ User điều chỉnh góc chụp
   ├─ [Optional] Toggle flash
   └─ Tap shutter button → chụp ảnh
   
3. DetectionScreen
   ├─ Auto-detect 4 góc (TFLite → fallback Contour)
   ├─ Hiển thị 4 drag handles
   ├─ [Optional] User kéo thả để điều chỉnh góc
   ├─ [Optional] Tap "Detect lại"
   ├─ [Optional] Tap "Reset góc"
   └─ Tap "Xác nhận"
   
4. EditScreen
   ├─ Perspective transform + preview hiển thị
   ├─ [Optional] Chọn filter (Gốc/B&W/Màu/Tối)
   ├─ [Optional] Điều chỉnh brightness/contrast
   ├─ [Optional] Xoay 90°
   └─ Tap "Lưu trang"
   
5. DocumentScreen
   ├─ Trang vừa lưu hiển thị trong list
   ├─ [Optional] Thêm trang → quay lại CameraScreen
   ├─ [Optional] Reorder trang
   └─ Tap export icon hoặc navigate to ExportScreen
   
6. ExportScreen
   ├─ Chọn format (PDF mặc định)
   └─ Tap "Lưu vào thiết bị" hoặc "Chia sẻ"
   
7. System Share Sheet / Files
   └─ User chia sẻ hoặc lưu file
```

## Success State
- File PDF được tạo và lưu/chia sẻ thành công
- Snackbar: "Đã lưu" hoặc share sheet mở

## Error States

| Lỗi | Xử lý |
|---|---|
| Camera permission denied | PermissionView với nút "Cấp quyền" / "Mở Cài đặt" |
| Detection thất bại | Fallback manual mode, toast "Không tìm thấy tài liệu. Chỉnh 4 góc thủ công." |
| Lưu trang thất bại | Error text trong EditScreen, user có thể thử lại |
| Export thất bại | Snackbar với message lỗi |
| Bộ nhớ đầy | Toast thông báo, hủy thao tác |

## Permission Handling

### Camera
- Android: `rememberPermissionState(CAMERA)` — hỏi khi mở CameraScreen
- iOS: `AVCaptureDevice.requestAccess(for: .video)`
- Nếu denied: hiển thị `PermissionView`

## Offline Behavior
- Toàn bộ flow hoạt động offline
- Không có network request nào

## Android / iOS Parity

| Bước | Android | iOS | Khác biệt |
|---|---|---|---|
| Camera | CameraX | AVFoundation | Implementation khác, behavior giống |
| Permission dialog | Native Android | Native iOS | System dialog — không custom |
| Detection | TFLite + OpenCV | TFLite + OpenCV (hoặc Vision framework) | Cần port |
| Image processing | Android Bitmap API | Core Image / UIKit | Cần port |
| PDF export | android.graphics.pdf | PDFKit | Cần port |
| Share | Intent.ACTION_SEND | UIActivityViewController | System sheet |

## Acceptance Criteria
- [ ] Từ HomeScreen → Camera → Detection → Edit → Document → Export trong 1 flow liên tục
- [ ] Back ở mỗi bước quay về bước trước đúng
- [ ] Detection auto-detect hiển thị 4 handles đúng vị trí
- [ ] Filter áp dụng và preview cập nhật trong 500ms
- [ ] PDF xuất ra có đúng số trang
- [ ] Share sheet mở với file đúng
