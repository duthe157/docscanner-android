# Technical Principles — CamScanner App

## Kiến trúc hiện tại

Project Android dùng **MVVM + Clean Architecture** với 4 layer:

```
UI Layer (Jetpack Compose Screens)
    ↕ State / Events
ViewModel Layer
    ↕ suspend fun / Flow
Domain Layer (UseCases + Models)
    ↕ Repository interfaces
Data Layer (Room + FileSystem + TFLite)
```

## Stack công nghệ (Android)

| Thành phần | Thư viện | Ghi chú |
|---|---|---|
| UI | Jetpack Compose + Material3 | Không dùng XML layout |
| Camera | CameraX | Lifecycle-aware |
| Xử lý ảnh | Android Bitmap API + OpenCV imgproc | Chỉ dùng imgproc + core |
| Edge detection | TFLite runtime + ContourDetector | TFLite → fallback Contour |
| PDF | Android PdfDocument API | Built-in, không cần thư viện ngoài |
| Database | Room (SQLite) | Metadata tài liệu |
| DI | Hilt | Singleton scope cho TFLite, DB |
| Async | Kotlin Coroutines + Flow | Dispatchers.Default cho CPU, IO cho disk |
| Image loading | Coil | Cache thumbnail tự động |
| Navigation | Navigation Compose | Single Activity |
| Permissions | Accompanist Permissions | Camera, READ_MEDIA_IMAGES |

## Nguyên tắc kỹ thuật

### Giữ nguyên business logic
- **Không rewrite** logic scanner/camera/crop/PDF/share nếu không cần thiết.
- Thay đổi UI **không được làm hỏng** pipeline: detection → transform → filter → export.
- Khi cập nhật UI, chỉ thay đổi Composable và theme — không chạm vào UseCase, Repository, Data layer.

### Threading model
```
Main Thread:    UI rendering, user events, StateFlow collection
Default Thread: CPU-intensive — detection, perspective transform, filter
IO Thread:      File read/write, Room queries, TFLite model load
```

### Memory management
- Không giữ Bitmap trong ViewModel — chỉ giữ URI/path.
- Gọi `bitmap.recycle()` ngay sau khi lưu xong.
- Dùng `BitmapFactory.Options.inSampleSize` khi decode ảnh lớn.
- Preview dùng ảnh resize 800px max, không dùng full resolution.

## Đơn vị đo lường

| Nền tảng | Layout | Text | Nguồn |
|---|---|---|---|
| Android | `dp` | `sp` | Figma unit × 1 = dp |
| iOS (tương lai) | `pt` | `pt` | Figma unit × 1 = pt |
| Figma | unit | unit | Source of truth |

**Quy tắc mapping:**
- 1 Figma unit = 1 Android dp = 1 iOS pt
- Không target physical pixel của thiết bị
- Không hardcode giá trị pixel (px) trong layout

## Design Tokens

- Shared design tokens là nguồn chuẩn cho mọi giá trị UI.
- File gốc: `design/tokens/base.tokens.json`
- Android mapping: `design/tokens/android.tokens.md`
- iOS mapping (tương lai): `design/tokens/ios.tokens.md`
- Mọi màu sắc, spacing, radius, typography phải đến từ token — không hardcode.

## Cấu trúc file Android (Compose)

```
presentation/
├── {screen}/
│   ├── {Screen}Screen.kt      ← Composable, mostly stateless
│   └── {Screen}ViewModel.kt   ← State holder, side effects, navigation
```

- **Route composable**: xử lý ViewModel injection, navigation, side effects.
- **Screen composable**: nhận state và callbacks, mostly stateless, dễ preview.
- **Components**: tách ra `presentation/components/` nếu dùng ở nhiều nơi.

## Những gì KHÔNG được làm

- Không thêm OCR, cloud, login, subscription, ads, server API, AI feature khi chưa được yêu cầu.
- Không thêm business logic vào Activity/Fragment/Composable.
- Không dùng `px` trong layout — chỉ dùng `dp`/`sp`.
- Không hardcode màu sắc, spacing, radius trực tiếp trong Composable.
- Không giữ Bitmap lớn trong memory lâu hơn cần thiết.
- Không block Main thread khi xử lý ảnh.
