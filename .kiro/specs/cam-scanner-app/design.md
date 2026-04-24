# Solution Architecture Document — CamScanner App

> **Tài liệu duy nhất của dự án.** Mọi thay đổi kiến trúc, công nghệ, module đều được cập nhật vào đây.  
> Nền tảng: **Android Native (Kotlin)** — tối ưu nhẹ nhất có thể.  
> Phiên bản: 1.0 | Cập nhật lần cuối: khởi tạo

---

## 1. Tổng quan kiến trúc

### 1.1 Mô hình kiến trúc

Ứng dụng theo kiến trúc **MVVM + Clean Architecture** với 4 layer rõ ràng:

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer                            │
│  Jetpack Compose Screens + Navigation                   │
│  HomeScreen │ CameraScreen │ EditScreen │ DocumentScreen│
└──────────────────────┬──────────────────────────────────┘
                       │ State / Events
┌──────────────────────▼──────────────────────────────────┐
│                  ViewModel Layer                        │
│  HomeViewModel │ CameraViewModel │ EditViewModel        │
│  DocumentViewModel │ ExportViewModel                    │
└──────────────────────┬──────────────────────────────────┘
                       │ suspend fun / Flow
┌──────────────────────▼──────────────────────────────────┐
│                  Domain Layer (UseCases)                │
│  ScanDocumentUseCase │ DetectEdgesUseCase               │
│  ProcessImageUseCase │ ExportDocumentUseCase            │
│  ManageDocumentUseCase │ StorageUseCase                 │
└──────────────────────┬──────────────────────────────────┘
                       │ Repository interfaces
┌──────────────────────▼──────────────────────────────────┐
│                  Data Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐ │
│  │  Room (SQLite│  │  FileSystem  │  │  TFLite       │ │
│  │  metadata)   │  │  (images/PDF)│  │  Inference    │ │
│  └──────────────┘  └──────────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 1.2 Luồng chính

```
Mở App
  │
  ├─► [Scan mới] ──► CameraScreen ──► chụp ảnh
  │                                       │
  └─► [Import ảnh] ──► Picker ────────────┤
                                          ▼
                               DetectionScreen
                               (auto-detect 4 góc)
                                          │
                               EditScreen (kéo góc,
                               filter, brightness)
                                          │
                               DocumentScreen
                               (quản lý trang)
                                          │
                               ExportScreen
                               (PDF / JPG / Share)
```

---

## 2. Công nghệ & Thư viện

Nguyên tắc: **chỉ dùng những gì thực sự cần, ưu tiên Android SDK built-in**.

| Thành phần | Thư viện | Lý do chọn |
|---|---|---|
| UI | Jetpack Compose | Modern, ít boilerplate, không cần XML layout |
| Camera | CameraX (Jetpack) | Official, nhẹ, hỗ trợ lifecycle tự động |
| Xử lý ảnh | Android Bitmap API + RenderScript (API < 31) / ImageProcessor | Built-in, không cần thư viện ngoài cho grayscale/blur/sharpen |
| Edge detection | OpenCV Android SDK (chỉ module `imgproc`) | Cần Canny edge + findContours — không có built-in tương đương; chỉ import module cần thiết (~4MB) |
| TFLite | TensorFlow Lite Android runtime | Chỉ runtime, không full TF (~1MB AAR) |
| PDF | Android PdfDocument API | Built-in hoàn toàn, không cần thư viện ngoài |
| Database | Room (SQLite wrapper) | Official Jetpack, type-safe, coroutines support |
| DI | Hilt | Official Google DI, ít boilerplate |
| Async | Kotlin Coroutines + Flow | Standard Kotlin, không thêm dependency |
| Image loading/cache | Coil | Nhẹ nhất trong các image loader (~1.5MB), Kotlin-first |
| Navigation | Navigation Compose | Official Jetpack |

> **Lưu ý OpenCV**: Chỉ import `opencv-android` module `imgproc` + `core` thông qua Maven. Không dùng full OpenCV SDK. Nếu sau này muốn loại bỏ OpenCV, có thể thay bằng custom Canny implementation thuần Java/Kotlin.

---

## 3. Cấu trúc Project

```
app/
├── src/main/
│   ├── assets/
│   │   └── models/
│   │       └── document_detector.tflite   ← ĐẶT FILE TFLITE Ở ĐÂY
│   ├── java/com/example/camscanner/
│   │   ├── di/
│   │   │   ├── AppModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   └── TFLiteModule.kt
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── DocumentDao.kt
│   │   │   │   │   └── PageDao.kt
│   │   │   │   ├── entity/
│   │   │   │   │   ├── DocumentEntity.kt
│   │   │   │   │   └── PageEntity.kt
│   │   │   │   └── FileStorageManager.kt
│   │   │   ├── ml/
│   │   │   │   ├── TFLiteDetector.kt
│   │   │   │   └── ContourDetector.kt
│   │   │   └── repository/
│   │   │       ├── DocumentRepositoryImpl.kt
│   │   │       └── StorageRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Document.kt
│   │   │   │   ├── Page.kt
│   │   │   │   ├── DetectionResult.kt
│   │   │   │   └── ExportOptions.kt
│   │   │   ├── repository/
│   │   │   │   ├── DocumentRepository.kt
│   │   │   │   └── StorageRepository.kt
│   │   │   └── usecase/
│   │   │       ├── DetectEdgesUseCase.kt
│   │   │       ├── ProcessImageUseCase.kt
│   │   │       ├── ExportDocumentUseCase.kt
│   │   │       └── ManageDocumentUseCase.kt
│   │   ├── presentation/
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   └── HomeViewModel.kt
│   │   │   ├── camera/
│   │   │   │   ├── CameraScreen.kt
│   │   │   │   └── CameraViewModel.kt
│   │   │   ├── detection/
│   │   │   │   ├── DetectionScreen.kt
│   │   │   │   └── DetectionViewModel.kt
│   │   │   ├── edit/
│   │   │   │   ├── EditScreen.kt
│   │   │   │   └── EditViewModel.kt
│   │   │   ├── document/
│   │   │   │   ├── DocumentScreen.kt
│   │   │   │   └── DocumentViewModel.kt
│   │   │   ├── export/
│   │   │   │   ├── ExportScreen.kt
│   │   │   │   └── ExportViewModel.kt
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   ├── util/
│   │   │   ├── ImageUtils.kt
│   │   │   ├── PerspectiveTransform.kt
│   │   │   └── BitmapCache.kt
│   │   └── MainActivity.kt
│   └── res/
│       └── ...
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 4. Chi tiết từng Module

### 4.1 Camera_Module

**Class chính:** `CameraScreen.kt`, `CameraViewModel.kt`

**Trách nhiệm:**
- Khởi tạo CameraX `Preview` + `ImageCapture` use case
- Quản lý lifecycle camera (tự động release khi app vào background)
- Điều khiển flash (`ImageCapture.FlashMode`)
- Auto-focus liên tục (`FocusMeteringAction`)
- Chụp ảnh → lưu tạm vào `FileStorageManager.tempDir` → emit URI sang DetectionViewModel

**Data flow:**
```
CameraScreen
  └─► CameraViewModel.capturePhoto()
        └─► ImageCapture.takePicture(outputFileOptions, executor, callback)
              └─► onImageSaved(outputFileResults) → emit tempImageUri
                    └─► navigate to DetectionScreen(tempImageUri)
```

**Interface chính:**
```kotlin
interface CameraController {
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView)
    suspend fun capturePhoto(): Uri
    fun toggleFlash()
    fun setAutoFocus(x: Float, y: Float)
}
```

---

### 4.2 Detection_Module

**Class chính:** `DetectionScreen.kt`, `DetectionViewModel.kt`, `DetectEdgesUseCase.kt`, `TFLiteDetector.kt`, `ContourDetector.kt`

**Trách nhiệm:**
- Nhận ảnh đầu vào (URI)
- Chạy pipeline phát hiện biên (TFLite → fallback Contour)
- Trả về `DetectionResult` gồm 4 điểm góc
- Hiển thị 4 điểm kéo thả trên ảnh preview

**Data flow:**
```
DetectionViewModel.detectEdges(imageUri)
  └─► DetectEdgesUseCase.execute(bitmap)
        ├─► TFLiteDetector.detect(bitmap)  [background thread]
        │     ├─► success → return 4 corners
        │     └─► failure → ContourDetector.detect(bitmap)
        └─► emit DetectionResult → UI cập nhật 4 drag handles
```

**Interface chính:**
```kotlin
interface EdgeDetector {
    suspend fun detect(bitmap: Bitmap): DetectionResult
}

data class DetectionResult(
    val corners: List<PointF>,  // [topLeft, topRight, bottomRight, bottomLeft]
    val confidence: Float,
    val method: DetectionMethod  // TFLITE | CONTOUR | MANUAL
)
```

---

### 4.3 Edit_Module

**Class chính:** `EditScreen.kt`, `EditViewModel.kt`, `ProcessImageUseCase.kt`, `PerspectiveTransform.kt`

**Trách nhiệm:**
- Perspective transform từ 4 điểm góc
- Áp dụng filter (Original / B&W / Color / Dark)
- Điều chỉnh brightness/contrast
- Xoay ảnh 90°
- Preview ở độ phân giải thấp, render full quality khi lưu

**Pipeline xử lý:**
```
Input bitmap (full res)
  │
  ├─► [Preview path] resize to 800px max → apply transform → apply filter → show
  │
  └─► [Save path] full res → apply transform → apply filter → compress JPEG 90%
```

**Filter implementation** (thuần Bitmap API, không cần thư viện):
- `Original`: không thay đổi
- `B&W (Scan)`: ColorMatrix grayscale + tăng contrast
- `Color (Enhanced)`: ColorMatrix saturation boost
- `Dark`: ColorMatrix brightness giảm + contrast tăng

---

### 4.4 Document_Module

**Class chính:** `DocumentScreen.kt`, `DocumentViewModel.kt`, `ManageDocumentUseCase.kt`

**Trách nhiệm:**
- CRUD tài liệu và trang
- Sắp xếp trang bằng drag-and-drop (Compose `LazyColumn` + `reorderable`)
- Merge tài liệu
- Đặt tên mặc định theo timestamp

---

### 4.5 Export_Module

**Class chính:** `ExportScreen.kt`, `ExportViewModel.kt`, `ExportDocumentUseCase.kt`

**Trách nhiệm:**
- Tạo PDF bằng `android.graphics.pdf.PdfDocument`
- Xuất JPG/PNG bằng `Bitmap.compress()`
- Mở Share Sheet qua `Intent.ACTION_SEND`
- Lưu vào Files qua `MediaStore` hoặc SAF
- Lưu vào Photos qua `MediaStore.Images`

---

### 4.6 Storage_Module

**Class chính:** `FileStorageManager.kt`, `DocumentRepositoryImpl.kt`, `StorageRepositoryImpl.kt`

**Cấu trúc thư mục trên thiết bị:**
```
/data/data/com.example.camscanner/files/
├── originals/          ← ảnh gốc chưa xử lý
│   └── {pageId}_orig.jpg
├── processed/          ← ảnh đã perspective transform + filter
│   └── {pageId}_proc.jpg
├── previews/           ← thumbnail 200px cho danh sách
│   └── {pageId}_thumb.jpg
└── exports/            ← PDF/ảnh xuất tạm thời
    └── {docId}.pdf
```

---

### 4.7 Settings_Module

**Class chính:** `SettingsScreen.kt`, `SettingsViewModel.kt`

**Lưu trữ:** `SharedPreferences` (không cần Room cho settings đơn giản)

**Các setting:**
- `default_export_format`: PDF | JPG | PNG
- `default_filter`: ORIGINAL | BW | COLOR | DARK
- Hiển thị dung lượng đang dùng (tính từ thư mục app)
- Xóa ảnh gốc (giữ processed)

---

## 5. TFLite Integration

### 5.1 Đặt file model

```
app/src/main/assets/models/document_detector.tflite
```

Đây là đường dẫn **cố định**. Khi thay model mới, chỉ cần thay file tại đường dẫn này — không cần sửa code (tên file không đổi).

### 5.2 Load model

```kotlin
// TFLiteModule.kt (Hilt)
@Provides
@Singleton
fun provideTFLiteInterpreter(@ApplicationContext context: Context): Interpreter {
    val modelBuffer = FileUtil.loadMappedFile(context, "models/document_detector.tflite")
    val options = Interpreter.Options().apply {
        numThreads = 2
        useNNAPI = false  // tắt NNAPI để tránh overhead khởi động
    }
    return Interpreter(modelBuffer, options)
}
```

### 5.3 Input/Output format (đã xác nhận với model thực tế)

| | Thông số |
|---|---|
| Input shape | `[1, 416, 416, 3]` — batch=1, H=416, W=416, RGB |
| Input type | `Float32`, normalize về `[0.0, 1.0]` |
| Output shape | `[1, 17, 3549]` — YOLO Pose format |
| Output type | `Float32` |

**Giải thích output `[1, 17, 3549]`:**
- 3549 = số anchor predictions
- 17 values mỗi anchor:
  - `[0]` cx, `[1]` cy, `[2]` w, `[3]` h — bounding box (normalized)
  - `[4]` objectness confidence
  - `[5..7]` keypoint 0 (top-left): x, y, conf
  - `[8..10]` keypoint 1 (top-right): x, y, conf
  - `[11..13]` keypoint 2 (bottom-right): x, y, conf
  - `[14..16]` keypoint 3 (bottom-left): x, y, conf

**Tiền xử lý:**
```kotlin
fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
    val resized = Bitmap.createScaledBitmap(bitmap, 416, 416, true)
    val buffer = ByteBuffer.allocateDirect(1 * 416 * 416 * 3 * 4)
    buffer.order(ByteOrder.nativeOrder())
    val pixels = IntArray(416 * 416)
    resized.getPixels(pixels, 0, 416, 0, 0, 416, 416)
    for (pixel in pixels) {
        buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)  // R
        buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)   // G
        buffer.putFloat((pixel and 0xFF) / 255f)            // B
    }
    return buffer
}
```

**Hậu xử lý YOLO Pose:**
```kotlin
fun parseOutput(output: Array<FloatArray>, imageWidth: Int, imageHeight: Int): List<PointF>? {
    // output shape: [17, 3549] — find anchor with highest objectness (row 4)
    val bestIdx = (0 until 3549).maxByOrNull { output[4][it] } ?: return null
    if (output[4][bestIdx] < 0.25f) return null  // confidence threshold

    // Extract 4 keypoints (rows 5-16, groups of 3)
    return (0..3).map { k ->
        PointF(output[5 + k*3][bestIdx] * imageWidth,
               output[6 + k*3][bestIdx] * imageHeight)
    }.let { orderCorners(it) }
}
```

### 5.4 Thay model mới

1. Thay file `document_detector.tflite` tại `assets/models/`
2. Nếu input/output shape thay đổi: cập nhật `preprocessBitmap()` và `postprocessOutput()` trong `TFLiteDetector.kt`
3. Nếu tên file thay đổi: cập nhật constant `MODEL_PATH = "models/document_detector.tflite"` trong `TFLiteDetector.kt`

### 5.5 Fallback strategy

```
TFLiteDetector.detect()
  ├─► try: load interpreter → preprocess → runInference → postprocess → validate corners
  │         validate: 4 điểm, tạo thành tứ giác lồi, diện tích > 10% ảnh
  └─► catch (any exception) → ContourDetector.detect()
        ├─► success → return corners
        └─► failure → return DetectionResult(method=MANUAL) → user chỉnh tay
```

---

## 6. Pipeline xử lý ảnh

### 6.1 Pipeline đầy đủ

```
[Input: Bitmap từ Camera/Gallery]
        │
        ▼
Step 1: RESIZE TẠM (cho detection)
        ImageUtils.resizeForDetection(bitmap, maxSize=1024)
        │
        ▼
Step 2: DETECT EDGES
        DetectEdgesUseCase → TFLiteDetector | ContourDetector
        Output: List<PointF> (4 góc)
        │
        ▼
Step 3: USER ADJUST (optional)
        EditScreen drag handles → updated List<PointF>
        │
        ▼
Step 4: PERSPECTIVE TRANSFORM
        PerspectiveTransform.warpPerspective(bitmap, corners)
        Dùng OpenCV Imgproc.getPerspectiveTransform + warpPerspective
        Output: Bitmap (đã làm phẳng)
        │
        ▼
Step 5: POST-PROCESS
        ImageUtils.sharpen(bitmap)     ← ConvolutionMatrix 3x3
        ImageUtils.denoise(bitmap)     ← Gaussian blur nhẹ
        │
        ▼
Step 6: APPLY FILTER
        ImageUtils.applyFilter(bitmap, filterType)
        ← ColorMatrix via ColorMatrixColorFilter
        │
        ▼
Step 7: ADJUST BRIGHTNESS/CONTRAST
        ImageUtils.adjustBrightnessContrast(bitmap, brightness, contrast)
        ← ColorMatrix
        │
        ▼
Step 8: SAVE
        [Preview] compress JPEG 60% → previews/{pageId}_thumb.jpg (200px)
        [Processed] compress JPEG 90% → processed/{pageId}_proc.jpg
        [Original] giữ nguyên → originals/{pageId}_orig.jpg
```

### 6.2 Threading model

```
UI Thread (Main)
  └─► launch(Dispatchers.IO) {
        val result = withContext(Dispatchers.Default) {
            // CPU-intensive: detection, transform, filter
        }
        withContext(Dispatchers.IO) {
            // I/O: save to disk
        }
        // emit result to StateFlow → UI updates on Main
      }
```

---

## 7. Data Models

### 7.1 Domain Models

```kotlin
// domain/model/Document.kt
data class Document(
    val id: String,           // UUID
    val name: String,
    val createdAt: Long,      // epoch ms
    val updatedAt: Long,
    val pages: List<Page>
)

// domain/model/Page.kt
data class Page(
    val id: String,           // UUID
    val documentId: String,
    val order: Int,
    val originalPath: String,
    val processedPath: String,
    val previewPath: String,
    val filter: FilterType,
    val brightness: Int,      // -100..100
    val contrast: Int,        // -100..100
    val rotation: Int,        // 0, 90, 180, 270
    val corners: List<PointF> // 4 góc đã xác nhận
)

enum class FilterType { ORIGINAL, BW, COLOR, DARK }

// domain/model/DetectionResult.kt
data class DetectionResult(
    val corners: List<PointF>,
    val confidence: Float,
    val method: DetectionMethod
)

enum class DetectionMethod { TFLITE, CONTOUR, MANUAL }

// domain/model/ExportOptions.kt
data class ExportOptions(
    val format: ExportFormat,
    val quality: Int = 90,    // JPEG quality
    val pageRange: IntRange? = null
)

enum class ExportFormat { PDF, JPG, PNG }
```

### 7.2 Room Entities

```kotlin
// data/local/entity/DocumentEntity.kt
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
)

// data/local/entity/PageEntity.kt
@Entity(
    tableName = "pages",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("documentId")]
)
data class PageEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageOrder: Int,
    val originalPath: String,
    val processedPath: String,
    val previewPath: String,
    val filter: String,       // FilterType.name
    val brightness: Int,
    val contrast: Int,
    val rotation: Int,
    val cornersJson: String   // JSON: [[x0,y0],[x1,y1],[x2,y2],[x3,y3]]
)
```

### 7.3 SQLite Schema

```sql
CREATE TABLE documents (
    id          TEXT PRIMARY KEY,
    name        TEXT NOT NULL,
    created_at  INTEGER NOT NULL,
    updated_at  INTEGER NOT NULL
);

CREATE TABLE pages (
    id              TEXT PRIMARY KEY,
    document_id     TEXT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    page_order      INTEGER NOT NULL,
    original_path   TEXT NOT NULL,
    processed_path  TEXT NOT NULL,
    preview_path    TEXT NOT NULL,
    filter          TEXT NOT NULL DEFAULT 'ORIGINAL',
    brightness      INTEGER NOT NULL DEFAULT 0,
    contrast        INTEGER NOT NULL DEFAULT 0,
    rotation        INTEGER NOT NULL DEFAULT 0,
    corners_json    TEXT NOT NULL
);

CREATE INDEX idx_pages_document_id ON pages(document_id);
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
```

---

## 8. Màn hình & Navigation

### 8.1 Danh sách màn hình

| Screen | Route | Mô tả |
|---|---|---|
| HomeScreen | `home` | Danh sách tài liệu, nút Scan/Import |
| CameraScreen | `camera/{documentId?}` | Camera preview, chụp ảnh |
| DetectionScreen | `detection/{imageUri}` | Hiển thị 4 góc auto-detect, cho phép kéo thả |
| EditScreen | `edit/{pageId}` | Filter, brightness, contrast, xoay |
| DocumentScreen | `document/{documentId}` | Xem trang, sắp xếp, thêm/xóa |
| ExportScreen | `export/{documentId}` | Chọn format, xuất, chia sẻ |
| SettingsScreen | `settings` | Cài đặt app |

### 8.2 Navigation Graph

```
HomeScreen
  ├─► CameraScreen ──► DetectionScreen ──► EditScreen ──► DocumentScreen
  ├─► [Import] ──────► DetectionScreen ──► EditScreen ──► DocumentScreen
  ├─► DocumentScreen (tap existing doc)
  │     ├─► EditScreen (tap page)
  │     ├─► CameraScreen (add page)
  │     └─► ExportScreen
  └─► SettingsScreen
```

### 8.3 Bottom Navigation

HomeScreen có bottom bar đơn giản:
- **Tài liệu** (danh sách)
- **Scan** (FAB trung tâm, nổi bật)
- **Cài đặt**

---

## 9. Chiến lược tối ưu hiệu năng

### 9.1 Memory management

- Bitmap lớn (>4MB) luôn được xử lý trên `Dispatchers.Default`, không giữ reference trên Main thread
- Sau khi lưu xong, gọi `bitmap.recycle()` ngay lập tức
- `BitmapFactory.Options.inSampleSize` để decode ảnh ở kích thước phù hợp với màn hình
- Không giữ Bitmap trong ViewModel — chỉ giữ URI/path

### 9.2 Threading

```
Main Thread:    UI rendering, user events
Default Thread: Image processing (detection, transform, filter)
IO Thread:      File read/write, Room queries, TFLite model load
```

### 9.3 Cache

- **Preview cache**: Coil tự động cache thumbnail trong memory + disk
- **TFLite Interpreter**: Singleton, load 1 lần khi app start, giữ suốt vòng đời app
- **Room query cache**: Flow-based, Room tự emit khi data thay đổi

### 9.4 APK size optimization

- `abiFilters = ["arm64-v8a", "armeabi-v7a"]` — loại bỏ x86 (không cần cho production)
- OpenCV: chỉ import `opencv-android` qua Maven, không bundle native libs thừa
- TFLite: chỉ runtime AAR, không full TensorFlow
- ProGuard/R8 bật full optimization

### 9.5 Preview vs Full quality

- Mọi thao tác edit trên UI đều dùng ảnh resize 800px max
- Chỉ render full quality khi user nhấn "Lưu" hoặc "Xuất"
- Progress indicator hiển thị trong suốt quá trình render full quality

---

## 10. Error Handling

| Tình huống | Xử lý |
|---|---|
| Không có quyền camera | Dialog hướng dẫn vào Settings, không crash |
| Không có quyền thư viện ảnh | Dialog hướng dẫn vào Settings |
| TFLite load thất bại | Fallback ContourDetector, log warning |
| TFLite inference thất bại | Fallback ContourDetector, không crash |
| Contour không tìm được | Fallback manual mode (user chỉnh tay) |
| Bộ nhớ đầy khi lưu | Toast thông báo, hủy thao tác, không corrupt data |
| Xuất PDF thất bại | Snackbar với nút "Thử lại" |
| Room query lỗi | Emit error state qua Flow, hiển thị empty state |

---

## 11. Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Flash toggle là round-trip

*For any* trạng thái flash hiện tại, bật rồi tắt flash (toggle hai lần liên tiếp) phải trả về đúng trạng thái ban đầu.

**Validates: Requirements 1.5**

---

### Property 2: Detection trả về 4 điểm góc hợp lệ

*For any* bitmap đầu vào hợp lệ, Detection_Module phải trả về đúng 4 điểm góc tạo thành một tứ giác lồi (convex quadrilateral) với diện tích > 0, theo thứ tự topLeft → topRight → bottomRight → bottomLeft.

**Validates: Requirements 3.1, 3.2**

---

### Property 3: Fallback khi detection thất bại không crash app

*For any* ảnh đầu vào khiến TFLite inference thất bại hoặc Contour không tìm được tứ giác hợp lệ, hệ thống phải trả về `DetectionResult` với `method = MANUAL` mà không ném uncaught exception.

**Validates: Requirements 3.5, 9.5**

---

### Property 4: Reset corners khôi phục về kết quả detection ban đầu

*For any* tập hợp 4 điểm góc được phát hiện tự động và bất kỳ thay đổi nào người dùng thực hiện, nhấn Reset phải trả về đúng 4 điểm góc từ kết quả detection ban đầu (không thay đổi).

**Validates: Requirements 4.4**

---

### Property 5: Detection là deterministic (idempotent)

*For any* bitmap đầu vào, chạy detection hai lần liên tiếp trên cùng ảnh đó phải trả về kết quả tương đương (cùng 4 điểm góc với sai số không đáng kể).

**Validates: Requirements 4.5**

---

### Property 6: Perspective transform tạo ra bitmap hình chữ nhật

*For any* tập hợp 4 điểm góc hợp lệ tạo thành tứ giác lồi, perspective transform phải tạo ra một bitmap không null với tỷ lệ chiều rộng/chiều cao phù hợp với kích thước đích.

**Validates: Requirements 4.6**

---

### Property 7: Áp dụng filter luôn trả về bitmap hợp lệ

*For any* bitmap đầu vào và bất kỳ `FilterType` nào (ORIGINAL, BW, COLOR, DARK), áp dụng filter phải trả về một bitmap không null có cùng kích thước (width × height) với bitmap đầu vào.

**Validates: Requirements 5.3**

---

### Property 8: Image adjustment parameters trong range hợp lệ không crash

*For any* bitmap đầu vào và bất kỳ giá trị brightness trong [-100, 100] và contrast trong [-100, 100], áp dụng adjustment phải trả về bitmap hợp lệ không null mà không ném exception.

**Validates: Requirements 5.4, 5.5**

---

### Property 9: Xoay 4 lần 90° trả về ảnh gốc

*For any* bitmap đầu vào, xoay 4 lần liên tiếp theo chiều kim đồng hồ (mỗi lần 90°) phải tạo ra bitmap có cùng kích thước và nội dung pixel tương đương với bitmap ban đầu.

**Validates: Requirements 5.6**

---

### Property 10: Tên mặc định tài liệu mới đúng định dạng

*For any* thời điểm tạo tài liệu mới, tên mặc định được gán phải khớp với pattern `"Tài liệu [dd/MM/yyyy HH:mm]"` và không được là chuỗi rỗng.

**Validates: Requirements 6.2**

---

### Property 11: Đổi tên tài liệu là persistent (round-trip)

*For any* tài liệu đang tồn tại và bất kỳ tên hợp lệ nào (không rỗng), sau khi đổi tên và query lại từ database, tên trả về phải bằng tên mới đã đặt.

**Validates: Requirements 6.3**

---

### Property 12: Reorder trang bảo toàn tất cả trang

*For any* tài liệu có N trang và bất kỳ hoán vị nào của danh sách trang, sau khi reorder: (1) số trang vẫn là N, (2) tập hợp page IDs không thay đổi, (3) thứ tự mới được áp dụng đúng.

**Validates: Requirements 6.4**

---

### Property 13: Thêm trang tăng số trang lên đúng 1

*For any* tài liệu có N trang, sau khi thêm một trang mới, số trang phải là N+1 và trang mới phải có mặt trong danh sách.

**Validates: Requirements 6.5**

---

### Property 14: Xóa trang giảm số trang xuống đúng 1

*For any* tài liệu có N ≥ 1 trang, sau khi xóa một trang, số trang phải là N-1 và trang đã xóa không còn truy xuất được từ database.

**Validates: Requirements 6.6**

---

### Property 15: Merge tài liệu bảo toàn tất cả trang

*For any* hai tài liệu A (có M trang) và B (có N trang), sau khi merge thành tài liệu C: số trang của C phải là M+N và tập hợp page IDs của C phải là hợp của page IDs từ A và B.

**Validates: Requirements 6.7**

---

### Property 16: Danh sách tài liệu được sắp xếp theo updatedAt giảm dần

*For any* tập hợp tài liệu với các giá trị `updatedAt` khác nhau, danh sách trả về từ repository phải được sắp xếp sao cho `documents[i].updatedAt >= documents[i+1].updatedAt` với mọi i hợp lệ.

**Validates: Requirements 6.8**

---

### Property 17: Xuất PDF có đúng số trang

*For any* tài liệu có N trang, file PDF được xuất ra phải có đúng N trang (mỗi trang ảnh tương ứng một trang PDF).

**Validates: Requirements 7.1**

---

### Property 18: Xuất ảnh đúng định dạng được chọn

*For any* trang ảnh và bất kỳ `ExportFormat` nào (JPG hoặc PNG), file được xuất ra phải là file ảnh hợp lệ đúng định dạng đã chọn (kiểm tra magic bytes của file).

**Validates: Requirements 7.2**

---

### Property 19: Storage round-trip bảo toàn dữ liệu

*For any* metadata tài liệu (tên, danh sách trang, filter, brightness, contrast, rotation, corners), sau khi lưu vào Room database và query lại, tất cả các trường phải có giá trị bằng với giá trị đã lưu.

**Validates: Requirements 8.1, 8.2**

---

### Property 20: Preview nhỏ hơn ảnh processed

*For any* ảnh đã xử lý, kích thước file preview (thumbnail) phải nhỏ hơn kích thước file processed tương ứng.

**Validates: Requirements 8.5**

---

### Property 21: TFLite preprocessing tạo ByteBuffer đúng shape và range

*For any* bitmap đầu vào, ByteBuffer sau khi preprocess phải có kích thước đúng bằng `1 × 256 × 256 × 3 × 4` bytes và tất cả giá trị float phải nằm trong khoảng `[0.0, 1.0]`.

**Validates: Requirements 9.3**

---

### Property 22: TFLite postprocessing trả về 4 điểm trong bounds ảnh

*For any* output array hợp lệ từ TFLite model và kích thước ảnh gốc (width, height), 4 điểm góc sau postprocessing phải có tọa độ x trong `[0, width]` và y trong `[0, height]`.

**Validates: Requirements 9.4**

---

### Property 23: Settings round-trip bảo toàn giá trị

*For any* giá trị `ExportFormat` hoặc `FilterType` hợp lệ, sau khi lưu vào SharedPreferences và đọc lại, giá trị trả về phải bằng giá trị đã lưu.

**Validates: Requirements 11.1, 11.2**

---

### Property 24: Báo cáo dung lượng bằng tổng kích thước file

*For any* tập hợp file được lưu bởi app, dung lượng được báo cáo trong Settings phải bằng tổng kích thước thực tế của tất cả các file đó (sai số ≤ 1KB do rounding).

**Validates: Requirements 11.3**

---

### Property 25: Xóa ảnh gốc không ảnh hưởng đến ảnh processed

*For any* tập hợp tài liệu, sau khi thực hiện "Xóa ảnh gốc": (1) tất cả file tại `originals/` phải không còn tồn tại, (2) tất cả file tại `processed/` phải vẫn còn tồn tại và truy xuất được.

**Validates: Requirements 11.4**

---

## 12. Testing Strategy

### 12.1 Tổng quan

Chiến lược kiểm thử kép:
- **Unit tests**: Kiểm tra các ví dụ cụ thể, edge cases, error conditions
- **Property-based tests**: Kiểm tra các universal properties trên nhiều input ngẫu nhiên
- **Integration tests**: Kiểm tra tích hợp với Room, FileSystem, MediaStore

### 12.2 Property-Based Testing

**Thư viện**: [Kotest](https://kotest.io/) với module `kotest-property` — Kotlin-first, hỗ trợ Compose testing, nhẹ hơn QuickCheck port.

**Cấu hình**: Mỗi property test chạy tối thiểu **100 iterations**.

**Tag format**: `// Feature: cam-scanner-app, Property {N}: {property_text}`

**Các property cần implement** (xem Section 11):
- Properties 1–25 được implement thành property-based tests tương ứng
- Mỗi property = 1 test function với `checkAll(iterations = 100, ...)`

**Ví dụ:**
```kotlin
// Feature: cam-scanner-app, Property 7: Áp dụng filter luôn trả về bitmap hợp lệ
@Test
fun `applying any filter to any bitmap returns valid bitmap with same dimensions`() = runTest {
    checkAll(iterations = 100, Arb.bitmap(), Arb.enum<FilterType>()) { bitmap, filterType ->
        val result = ImageUtils.applyFilter(bitmap, filterType)
        result shouldNotBe null
        result.width shouldBe bitmap.width
        result.height shouldBe bitmap.height
    }
}
```

### 12.3 Unit Tests

Tập trung vào:
- Navigation flows (example-based)
- Error handling scenarios
- UI state transitions
- Edge cases không cover được bởi property generators

### 12.4 Integration Tests

- Room database CRUD operations
- FileStorageManager read/write
- MediaStore save operations (cần emulator/device)
- TFLite model load từ assets

### 12.5 Test Coverage Targets

| Layer | Target |
|---|---|
| Domain (UseCases) | ≥ 90% |
| Data (Repository, Detectors) | ≥ 80% |
| Presentation (ViewModel) | ≥ 70% |
| UI (Compose) | Key flows only |


---

## 13. Training Data Collection (Planned)

### 13.1 Mục tiêu

Thu thập data từ người dùng thực để cải thiện TFLite model theo thời gian (active learning loop).

### 13.2 Data schema

```json
{
  "image_id": "uuid-v4",
  "timestamp": "ISO-8601",
  "image_path": "gdrive://CamScanner-Training/{uuid}.jpg",
  "auto_corners": [[x1,y1],[x2,y2],[x3,y3],[x4,y4]],
  "user_corners": [[x1,y1],[x2,y2],[x3,y3],[x4,y4]],
  "detection_method": "TFLITE|CONTOUR|MANUAL",
  "correction_delta": 45.2,
  "image_width": 1280,
  "image_height": 960,
  "app_version": "1.0.0"
}
```

### 13.3 Thu thập logic

- **Khi nào lưu**: Chỉ lưu khi `correction_delta > threshold` (user thực sự sửa góc)
  - `correction_delta` = tổng khoảng cách Euclidean giữa auto_corners và user_corners
  - Threshold gợi ý: 50px trở lên (tránh lưu data nhiễu khi user không chỉnh)
- **Async**: Upload chạy trên background thread sau khi user confirm, không block UX
- **Privacy**: Cần xin phép user rõ ràng (opt-in) trước khi upload

### 13.4 Storage backend

**Google Drive** (user cấp OAuth2 permission):
- Ảnh → folder `CamScanner-Training/images/{uuid}.jpg`
- Metadata → Google Sheets hoặc file JSON trong Drive

**Implementation status**: `PLANNED` — chưa implement, sẽ làm sau khi core features ổn định.

### 13.5 Retraining workflow

```
App collects (image, auto_corners, user_corners)
    ↓ async upload to Google Drive
Google Drive folder accumulates data
    ↓ manual trigger (developer)
Python training script (project AI-Training/document-scanner)
    ↓ train YOLO Pose model
Export → document_detector.tflite
    ↓ update assets/models/document_detector.tflite in app
Release new app version
```

### 13.6 Files cần implement (future)

```
data/
  collector/
    TrainingDataCollector.kt   — thu thập & queue data locally
    GoogleDriveUploader.kt     — upload async lên Drive
    TrainingDataEntity.kt      — Room entity lưu queue offline
```
