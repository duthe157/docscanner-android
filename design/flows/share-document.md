# Flow: Share Document

> **Status**: Implemented on Android ✅

## Goal
User xuất và chia sẻ tài liệu qua các app khác.

## Entry Points
- DocumentScreen → TopAppBar → Export icon (📤)
- HomeScreen → DocumentCard → More menu → (future: Chia sẻ)

## Steps

```
1. DocumentScreen
   └─ Tap export icon trong TopAppBar
   
2. ExportScreen
   ├─ Hiển thị thumbnails các trang
   ├─ Chọn format (PDF mặc định từ Settings)
   └─ Tap "Chia sẻ"
   
3. Export processing
   ├─ Progress indicator hiển thị
   └─ File được tạo trong exports/ directory
   
4. System Share Sheet
   └─ User chọn app để chia sẻ (email, messaging, cloud, v.v.)
```

## Success State
- Share sheet mở với file đúng format
- User chia sẻ thành công qua app họ chọn

## Error States
- Export thất bại: Snackbar với message + nút retry

## Offline Behavior
- Export hoạt động offline
- Share phụ thuộc vào app đích (email cần internet, messaging app có thể offline)

## Android / iOS Parity

| Element | Android | iOS |
|---|---|---|
| Share | `Intent.ACTION_SEND` | `UIActivityViewController` |
| File URI | `FileProvider` content URI | `URL(fileURLWithPath:)` |
| PDF creation | `android.graphics.pdf.PdfDocument` | `PDFKit` |

## Acceptance Criteria
- [ ] ExportScreen hiển thị thumbnails đúng
- [ ] Format selector hoạt động
- [ ] Progress indicator hiển thị khi export
- [ ] Share sheet mở với file đúng
- [ ] File có thể mở được từ app nhận
