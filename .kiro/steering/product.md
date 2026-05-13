# Product Overview — CamScanner App

## Mô tả sản phẩm

CamScanner App là ứng dụng scan tài liệu đơn giản, chạy hoàn toàn offline trên thiết bị di động.

**Mục tiêu chính của người dùng:**
- Scan giấy tờ/tài liệu bằng camera hoặc import từ thư viện ảnh
- Tự động phát hiện và căn chỉnh biên tài liệu
- Áp dụng bộ lọc để ảnh scan rõ nét hơn
- Lưu lại và chia sẻ dưới dạng PDF hoặc ảnh

## Luồng chính

```
Home → Camera Scan → Crop/Detect → Preview/Edit → Save Document → Export PDF → Share
```

Hoặc:

```
Home → Import từ thư viện → Crop/Detect → Preview/Edit → Save Document → Export PDF → Share
```

## Nguyên tắc sản phẩm

- **Offline-first**: Toàn bộ xử lý và lưu trữ diễn ra trên thiết bị, không cần kết nối mạng.
- **Đơn giản**: Giao diện tối giản, tập trung vào luồng scan → lưu → chia sẻ.
- **Nhanh**: Phản hồi tức thì, không block UI khi xử lý ảnh.
- **Tin cậy**: Không mất dữ liệu, fallback rõ ràng khi có lỗi.

## Phạm vi sản phẩm (Scope)

### Có trong scope
- Chụp ảnh tài liệu bằng camera
- Import ảnh từ thư viện thiết bị
- Tự động phát hiện biên tài liệu (TFLite + Contour)
- Chỉnh sửa 4 góc thủ công
- Perspective transform (làm phẳng tài liệu)
- Bộ lọc ảnh: Original, Đen trắng, Màu sắc, Tối
- Điều chỉnh độ sáng và tương phản
- Xoay ảnh
- Quản lý tài liệu nhiều trang
- Sắp xếp lại thứ tự trang
- Đổi tên tài liệu
- Xuất PDF
- Xuất ảnh JPG/PNG
- Chia sẻ qua Share Sheet của hệ điều hành
- Cài đặt cơ bản (format mặc định, filter mặc định)
- Xem dung lượng bộ nhớ đang dùng

### KHÔNG có trong scope (không thêm nếu chưa được yêu cầu)
- OCR (nhận dạng chữ)
- Cloud sync / backup
- Login / tài khoản người dùng
- Subscription / in-app purchase
- Quảng cáo (ads)
- Server API
- AI enhancement (ngoài TFLite model đã có)
- Chia sẻ qua link
- Collaboration / multi-user

## Nền tảng mục tiêu

- **Hiện tại**: Android (Kotlin, Jetpack Compose)
- **Tương lai**: iOS (SwiftUI hoặc UIKit — quyết định sau)

## Phiên bản

- Android minSdk: 26 (Android 8.0)
- Android targetSdk: 34 (Android 14)
