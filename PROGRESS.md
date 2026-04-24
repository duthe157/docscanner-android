# Tiến độ CamScanner App

**Cập nhật:** 18/04/2026

---

## ✅ Đã hoàn thành (đang chạy trên emulator)

| Module | Trạng thái | Chi tiết |
|--------|-----------|---------|
| Project setup | ✅ | AGP 8.2, Kotlin 1.9, JDK 17, Hilt, Room |
| OpenCV 4.8.0 | ✅ | PerspectiveTransform + ContourDetector ACTIVE |
| `libc++_shared.so` | ✅ | Copy từ NDK, 4 ABIs |
| TFLite model | ✅ | `document_detector.tflite` (~10MB) đã đặt đúng chỗ |
| TFLiteDetector | ✅ | Parse YOLO Pose output `[1,17,3549]` đúng |
| TFLiteModule | ✅ | Graceful fallback về ContourDetector |
| Domain layer | ✅ | Models, Repository interface, Use cases |
| Data layer | ✅ | Room entities, DAOs (+ getPageById), FileStorageManager |
| **DocumentRepositoryImpl** | ✅ | **MỚI** — mapping Entity↔Domain, CRUD, merge, reorder |
| **ManageDocumentUseCase** | ✅ | **MỚI** — create/rename/delete/addPage/removePage/reorder/merge |
| **ExportDocumentUseCase** | ✅ | **MỚI** — exportToPdf (multi-page), exportToImage (JPG/PNG) |
| Utilities | ✅ | ImageUtils, PerspectiveTransform (OpenCV), BitmapCache |
| HomeScreen | ✅ | **CẬP NHẬT** — danh sách tài liệu từ Room, delete confirm, FAB |
| **HomeViewModel** | ✅ | **MỚI** — observe documents Flow, delete with confirm |
| CameraScreen | ✅ | CameraX, flash toggle, permission |
| DetectionScreen | ✅ | Ảnh thực + 4 drag handles, OpenCV detection |
| EditScreen | ✅ | **CẬP NHẬT** — nút "Lưu" lưu vào DB, tạo document mới |
| EditViewModel | ✅ | **CẬP NHẬT** — saveToDocument(), lưu file + Room |
| **DocumentScreen** | ✅ | **MỚI** — danh sách trang, thumbnail, rename, delete page |
| **DocumentViewModel** | ✅ | **MỚI** — reorder, delete page, rename document |
| ExportScreen | ✅ | PDF/JPEG/PNG, lưu máy, chia sẻ |
| **SettingsScreen** | ✅ | **MỚI** — default filter, export format, storage info, xóa ảnh gốc |
| **SettingsViewModel** | ✅ | **MỚI** — SharedPreferences, calculateTotalSize, deleteOriginals |
| NavGraph | ✅ | **CẬP NHẬT** — Home→Camera→Detection→Edit→Document→Export→Settings |
| AppModule (DI) | ✅ | **CẬP NHẬT** — provide DocumentRepository |

---

## 🔬 TFLite Model — thông tin đã xác nhận

```
Model: YOLO Pose (document corner detection)
Input:  [1, 416, 416, 3]  float32  RGB normalized [0,1]
Output: [1, 17, 3549]     float32

Output layout (per anchor):
  [0-3]   bbox: cx, cy, w, h (normalized)
  [4]     objectness confidence
  [5-7]   keypoint 0 (top-left):     x, y, conf
  [8-10]  keypoint 1 (top-right):    x, y, conf
  [11-13] keypoint 2 (bottom-right): x, y, conf
  [14-16] keypoint 3 (bottom-left):  x, y, conf
```

**Detection chain hiện tại:**
```
TFLiteDetector (YOLO Pose, conf≥0.25)
    ↓ fallback nếu không đủ confidence
ContourDetector (OpenCV Canny + findContours)
    ↓ fallback nếu không tìm được quad
Default corners (8% margin từ mép ảnh)
```

---

## 📊 Tiến độ theo luồng

```
Scan flow:     HomeScreen → Camera → Detection → Edit → Document → Export  ✅ HOÀN THÀNH
TFLite:        Model load + YOLO Pose output parsing                        ✅ HOÀN THÀNH
OpenCV:        PerspectiveTransform + AdaptiveThreshold                     ✅ HOÀN THÀNH
Export:        PDF + JPEG + PNG + Share                                     ✅ HOÀN THÀNH
Document CRUD: Tạo/xem/xóa/đổi tên tài liệu, quản lý trang                ✅ HOÀN THÀNH
Settings:      Default filter, export format, storage info, xóa ảnh gốc   ✅ HOÀN THÀNH
Training data: ❌ Planned
```

---

## 🗓️ Planned (chưa implement)

| Feature | Mô tả | Ưu tiên |
|---------|-------|---------|
| Drag-and-drop reorder trang | Kéo thả trong DocumentScreen | Medium |
| Edit existing page | Chỉnh sửa trang đã lưu từ DocumentScreen | Medium |
| Export từ DocumentScreen | ExportScreen dùng documentId thay vì bitmap | Medium |
| Training data collector | Thu thập (ảnh, auto_corners, user_corners) khi user chỉnh góc | Low |
| Google Drive upload | Async upload training data lên Drive | Low |
| Property-based tests | 25 properties trong design.md | Low |

---

## 🚀 Chạy app

```bash
# Start emulator
emulator -avd Pixel_7

# Build & install
.\gradlew.bat assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.camscanner/.MainActivity
```
