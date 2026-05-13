# Copywriting Rules — CamScanner App

## Nguyên tắc chung

- Text trong app phải **ngắn, rõ, dễ hiểu** — người dùng không cần đọc kỹ để hiểu.
- Không dùng thuật ngữ kỹ thuật trong text hiển thị cho user.
- Dùng ngôn ngữ **hành động** cho button và CTA.
- Nhất quán — cùng một hành động phải dùng cùng một label ở mọi nơi trong app.

---

## Labels chuẩn (bắt buộc dùng nhất quán)

### Hành động chính

| Hành động | Label chuẩn | KHÔNG dùng |
|---|---|---|
| Bắt đầu scan | **Scan** | "Chụp ảnh", "Quét", "Camera" |
| Lưu tài liệu | **Lưu** | "Save", "Ghi lại", "Xác nhận lưu" |
| Xuất ra PDF | **Xuất PDF** | "Export PDF", "Tạo PDF", "Render PDF" |
| Chia sẻ file | **Chia sẻ** | "Share", "Gửi", "Forward" |
| Xóa | **Xóa** | "Delete", "Loại bỏ", "Hủy bỏ" |
| Đổi tên | **Đổi tên** | "Rename", "Sửa tên", "Chỉnh tên" |
| Hủy thao tác | **Hủy** | "Cancel", "Thoát", "Không" |
| Xác nhận xong | **Xong** | "Done", "OK", "Hoàn tất", "Xác nhận" |
| Thử lại | **Thử lại** | "Retry", "Làm lại", "Thực hiện lại" |
| Thêm trang | **Thêm trang** | "Add page", "Thêm ảnh", "Chụp thêm" |
| Import ảnh | **Import ảnh** | "Chọn ảnh", "Từ thư viện", "Gallery" |

### Hành động camera

| Hành động | Label chuẩn |
|---|---|
| Chụp ảnh | (không có label — chỉ dùng icon) |
| Bật/tắt flash | (không có label — chỉ dùng icon) |
| Chụp lại | **Chụp lại** |
| Xác nhận góc | **Xác nhận** |
| Đặt lại góc | **Đặt lại** |

### Bộ lọc ảnh

| FilterType | Label hiển thị |
|---|---|
| ORIGINAL | **Gốc** |
| BW | **Đen trắng** |
| COLOR | **Màu sắc** |
| DARK | **Tối** |

### Định dạng xuất

| ExportFormat | Label hiển thị |
|---|---|
| PDF | **PDF** |
| JPG | **Ảnh JPG** |
| PNG | **Ảnh PNG** |

---

## Empty States

| Màn hình | Title | Subtitle | Button |
|---|---|---|---|
| Home (chưa có tài liệu) | **Chưa có tài liệu nào** | Nhấn Scan để bắt đầu scan tài liệu đầu tiên | **Scan ngay** |
| Document (chưa có trang) | **Tài liệu trống** | Thêm trang để bắt đầu | **Thêm trang** |

---

## Error Messages

### Nguyên tắc viết error message
- Mô tả **điều gì đã xảy ra** (không phải lỗi kỹ thuật).
- Gợi ý **cách khắc phục** nếu có thể.
- Ngắn gọn — tối đa 2 câu.

| Tình huống | Message |
|---|---|
| Không có quyền camera | **Cần quyền truy cập camera.** Vào Cài đặt để cấp quyền. |
| Không có quyền thư viện ảnh | **Cần quyền truy cập ảnh.** Vào Cài đặt để cấp quyền. |
| Bộ nhớ đầy | **Không đủ dung lượng.** Xóa bớt file để tiếp tục. |
| Xuất PDF thất bại | **Không thể xuất file.** Thử lại hoặc khởi động lại app. |
| Không phát hiện được tài liệu | **Không tìm thấy tài liệu.** Chỉnh 4 góc thủ công. |
| Lỗi chung | **Có lỗi xảy ra.** Thử lại. |

---

## Confirmation Dialogs

### Xóa tài liệu
- **Title:** Xóa tài liệu?
- **Message:** Tài liệu này sẽ bị xóa vĩnh viễn và không thể khôi phục.
- **Confirm button:** Xóa (màu destructive)
- **Cancel button:** Hủy

### Xóa ảnh gốc (trong Settings)
- **Title:** Xóa ảnh gốc?
- **Message:** Ảnh gốc sẽ bị xóa để giải phóng dung lượng. Ảnh đã xử lý vẫn được giữ lại.
- **Confirm button:** Xóa (màu destructive)
- **Cancel button:** Hủy

### Hủy scan đang dở
- **Title:** Hủy scan?
- **Message:** Ảnh chưa được lưu sẽ bị mất.
- **Confirm button:** Hủy scan (màu destructive)
- **Cancel button:** Tiếp tục

---

## Tên mặc định tài liệu

Format: `Tài liệu dd/MM/yyyy HH:mm`

Ví dụ: `Tài liệu 15/01/2025 09:30`

---

## Quy tắc viết text

1. **Viết hoa chữ đầu câu** — không viết hoa toàn bộ (không dùng ALL CAPS).
2. **Không dùng dấu chấm than** trong UI text thông thường.
3. **Không dùng từ tiếng Anh** nếu có từ tiếng Việt tương đương rõ ràng.
4. **Không dùng jargon kỹ thuật**: không dùng "perspective transform", "TFLite", "contour detection", "bitmap", "URI" trong text hiển thị cho user.
5. **Số và đơn vị**: dùng định dạng Việt Nam (dấu phẩy cho phần thập phân, dấu chấm cho phân cách nghìn).
6. **Ngày giờ**: format `dd/MM/yyyy HH:mm`.
7. **Dung lượng file**: hiển thị theo đơn vị phù hợp (KB, MB, GB) với 1 chữ số thập phân.
