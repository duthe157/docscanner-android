# Flow Rules — CamScanner App

## Nguyên tắc chung

- Mỗi functional flow có file spec riêng trong `design/flows/`.
- Flow spec mô tả toàn bộ hành trình của user từ entry point đến success/error state.
- Flow phải hoạt động đúng trên cả Android và iOS (trừ các khác biệt platform đã được document).

---

## Cấu trúc bắt buộc của mỗi Flow Spec

Mỗi file `design/flows/*.md` phải có đầy đủ các section sau:

### 1. Goal
Mục tiêu của flow — user muốn đạt được gì.

### 2. Entry Points
Từ đâu user có thể bắt đầu flow này.

### 3. Steps
Từng bước trong flow, bao gồm:
- User action
- System response
- Screen transition (nếu có)

### 4. Success State
Trạng thái khi flow hoàn thành thành công.

### 5. Error States
Các trường hợp lỗi có thể xảy ra và cách xử lý.

### 6. Permission Handling
Quyền nào cần thiết và xử lý khi bị từ chối.

### 7. Offline Behavior
Flow hoạt động như thế nào khi offline (app này luôn offline-first).

### 8. Android / iOS Parity Notes
Những điểm khác biệt giữa 2 platform trong flow này.

### 9. Acceptance Criteria
Điều kiện để flow được coi là "done".

---

## Core Flows

### Flow 1: Scan to PDF
**File:** `design/flows/scan-to-pdf.md`

Luồng chính của app:
```
Home (nhấn Scan)
  → CameraScreen (chụp ảnh)
  → DetectionScreen (auto-detect 4 góc, user điều chỉnh)
  → EditScreen (filter, brightness, contrast, xoay)
  → DocumentScreen (xem trang, thêm trang nếu cần)
  → ExportScreen (chọn PDF, nhấn Export)
  → Share Sheet (chia sẻ hoặc lưu)
```

### Flow 2: Import Image to PDF
**File:** `design/flows/import-image-to-pdf.md`

```
Home (nhấn Import)
  → System Image Picker
  → DetectionScreen (auto-detect 4 góc)
  → EditScreen
  → DocumentScreen
  → ExportScreen
  → Share Sheet
```

### Flow 3: Share Document
**File:** `design/flows/share-document.md`

```
HomeScreen hoặc DocumentScreen (nhấn Share)
  → ExportScreen (chọn format)
  → System Share Sheet
```

### Flow 4: Delete Document
**File:** `design/flows/delete-document.md`

```
HomeScreen (long press hoặc swipe document card)
  → Confirmation Dialog
  → Xóa → quay về HomeScreen (document đã biến mất)
```

### Flow 5: Rename Document
**File:** `design/flows/rename-document.md`

```
DocumentScreen hoặc HomeScreen (tap tên document)
  → Inline edit hoặc Dialog
  → Lưu tên mới → cập nhật UI ngay lập tức
```

---

## Permission Behavior

### Camera Permission
- **Lần đầu mở CameraScreen**: Hệ thống tự động hỏi quyền (native dialog).
- **Nếu user cho phép**: Mở camera bình thường.
- **Nếu user từ chối**: Hiển thị `PermissionView` với nút "Mở Cài đặt".
- **Nếu user từ chối vĩnh viễn (Android)**: Hiển thị `PermissionView` với hướng dẫn vào Settings thủ công.
- **Không** tự động navigate vào Settings — chỉ hướng dẫn user.

### Storage / Media Permission
- **Android 13+**: Cần `READ_MEDIA_IMAGES` để import ảnh.
- **Android < 13**: Cần `READ_EXTERNAL_STORAGE`.
- **Xử lý tương tự camera**: Hỏi khi cần, hiển thị PermissionView nếu bị từ chối.
- **Lưu file**: Dùng `MediaStore` hoặc SAF — không cần permission riêng trên Android 10+.

---

## Loading / Empty / Error Behavior

### Loading State
- Hiển thị `LoadingState` component (spinner hoặc skeleton).
- Không block toàn bộ màn hình nếu có thể — chỉ block phần đang load.
- Xử lý ảnh nặng: hiển thị progress indicator với % nếu có thể.
- Timeout: nếu loading quá 30 giây → chuyển sang error state.

### Empty State
- Hiển thị `EmptyState` component với:
  - Icon minh họa
  - Message ngắn gọn
  - Action button (ví dụ: "Scan tài liệu đầu tiên")
- Không hiển thị empty state trong khi đang loading.

### Error State
- Hiển thị `ErrorState` component với:
  - Icon lỗi
  - Message mô tả lỗi (ngắn gọn, không dùng technical jargon)
  - Nút "Thử lại" nếu có thể retry
- Log lỗi chi tiết cho developer, hiển thị message thân thiện cho user.
- Không crash app — mọi lỗi phải được catch và hiển thị error state.

---

## Back Behavior

| Màn hình | Back action |
|---|---|
| HomeScreen | Exit app (hoặc minimize) |
| CameraScreen | Quay về HomeScreen, hủy scan hiện tại |
| DetectionScreen | Quay về CameraScreen (chụp lại) |
| EditScreen | Quay về DetectionScreen (chỉnh lại góc) |
| DocumentScreen | Quay về HomeScreen |
| ExportScreen | Quay về DocumentScreen |
| SettingsScreen | Quay về HomeScreen |

**Quy tắc:**
- Back từ giữa luồng scan → hỏi confirm nếu đã có dữ liệu chưa lưu.
- Back từ màn hình không có dữ liệu chưa lưu → navigate ngay, không hỏi.

---

## Offline-First Behavior

App này **luôn offline** — không có network request trong luồng chính.

- Mọi thao tác đọc/ghi đều dùng local storage (Room + FileSystem).
- Không hiển thị "No internet connection" — app không cần internet.
- Nếu bộ nhớ đầy: hiển thị thông báo rõ ràng, không crash, không mất dữ liệu hiện có.
- Nếu file bị corrupt: hiển thị error state cho item đó, không ảnh hưởng các item khác.

---

## Những gì KHÔNG được thêm vào flow

- OCR processing
- Cloud upload / sync
- Login / authentication gate
- Subscription paywall
- Ad interstitial
- Server API call
- AI enhancement (ngoài TFLite đã có)

Nếu cần thêm bất kỳ tính năng nào trong danh sách trên → phải có yêu cầu rõ ràng từ product owner trước.
