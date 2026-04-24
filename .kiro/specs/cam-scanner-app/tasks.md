# Kế hoạch triển khai: CamScanner App

## Tổng quan

Triển khai ứng dụng Android Native (Kotlin) theo kiến trúc MVVM + Clean Architecture với Jetpack Compose.
Ưu tiên luồng chính: **Camera → Detection → Edit → Document → Export**, sau đó mới đến các tính năng phụ.

Ngôn ngữ: **Kotlin**
Framework test: **Kotest** (property-based) + **JUnit5** (unit tests)

---

## Tasks

- [ ] 1. Thiết lập cấu trúc dự án và dependencies
  - [ ] 1.1 Cấu hình `build.gradle.kts` (app-level): thêm dependencies CameraX, OpenCV, TFLite, Room, Hilt, Coil, Navigation Compose, Kotest
    - Bật `buildFeatures { compose = true }`, cấu hình `composeOptions`, `abiFilters = ["arm64-v8a", "armeabi-v7a"]`
    - Thêm kapt/ksp cho Room và Hilt
    - _Requirements: 9.1_
  - [ ] 1.2 Cấu hình `build.gradle.kts` (project-level): Hilt classpath, Kotlin version, AGP version
    - _Requirements: 9.1_
  - [ ] 1.3 Tạo cấu trúc package: `di/`, `data/local/db/`, `data/local/entity/`, `data/ml/`, `data/repository/`, `domain/model/`, `domain/repository/`, `domain/usecase/`, `presentation/home/`, `presentation/camera/`, `presentation/detection/`, `presentation/edit/`, `presentation/document/`, `presentation/export/`, `presentation/settings/`, `util/`
    - _Requirements: 8.1_
  - [ ] 1.4 Tạo `MainActivity.kt` với `@AndroidEntryPoint`, `setContent { CamScannerApp() }`, xin quyền camera và storage khi khởi động
    - _Requirements: 1.7, 2.4_
  - [ ] 1.5 Tạo `CamScannerApplication.kt` với `@HiltAndroidApp`
    - _Requirements: 9.2_
  - [ ] 1.6 Cập nhật `AndroidManifest.xml`: permissions (`CAMERA`, `READ_MEDIA_IMAGES`, `WRITE_EXTERNAL_STORAGE`), FileProvider cho chia sẻ file, `CamScannerApplication` làm application class
    - _Requirements: 1.7, 2.4, 7.3_
  - [ ] 1.7 Đặt placeholder file `app/src/main/assets/models/document_detector.tflite` (file rỗng hoặc model thực tế) để đường dẫn assets tồn tại
    - _Requirements: 9.1_

- [ ] 2. Data layer — Entities, DAOs, Database
  - [ ] 2.1 Tạo `DocumentEntity.kt` và `PageEntity.kt` theo schema trong design (Section 7.2), bao gồm `@Entity`, `@PrimaryKey`, `@ForeignKey`, `@Index`
    - _Requirements: 8.2, 8.3_
  - [ ] 2.2 Tạo `DocumentDao.kt`: `insertDocument`, `updateDocument`, `deleteDocument`, `getDocumentById`, `getAllDocuments` (Flow, sắp xếp theo `updatedAt DESC`)
    - _Requirements: 6.8, 8.1_
  - [ ] 2.3 Tạo `PageDao.kt`: `insertPage`, `updatePage`, `deletePage`, `getPagesByDocumentId` (Flow), `deletePagesByDocumentId`
    - _Requirements: 6.5, 6.6, 8.1_
  - [ ] 2.4 Tạo `AppDatabase.kt` với `@Database(entities = [DocumentEntity::class, PageEntity::class], version = 1)`, export schema
    - _Requirements: 8.3_
  - [ ] 2.5 Tạo `DatabaseModule.kt` (Hilt): provide `AppDatabase` singleton, provide `DocumentDao`, provide `PageDao`
    - _Requirements: 8.3_

- [ ] 3. Data layer — FileStorageManager và Storage Repository
  - [ ] 3.1 Tạo `FileStorageManager.kt`: quản lý 4 thư mục (`originals/`, `processed/`, `previews/`, `exports/`), các hàm `saveOriginal`, `saveProcessed`, `savePreview`, `saveExport`, `deletePageFiles`, `getTempDir`, `calculateTotalSize`
    - _Requirements: 8.1, 8.5, 11.3_
  - [ ] 3.2 Tạo `StorageRepository.kt` (interface domain) và `StorageRepositoryImpl.kt` (data): wrap `FileStorageManager`, expose suspend functions
    - _Requirements: 8.1_
  - [ ] 3.3 Tạo `AppModule.kt` (Hilt): provide `FileStorageManager`, `StorageRepository`
    - _Requirements: 8.1_

- [ ] 4. Domain layer — Models và Repository interfaces
  - [ ] 4.1 Tạo domain models: `Document.kt`, `Page.kt`, `DetectionResult.kt`, `ExportOptions.kt`, enums `FilterType`, `DetectionMethod`, `ExportFormat` (theo Section 7.1 của design)
    - _Requirements: 3.1, 5.2, 7.1, 7.2_
  - [ ] 4.2 Tạo `DocumentRepository.kt` (interface): `getDocuments(): Flow<List<Document>>`, `getDocumentById`, `createDocument`, `updateDocument`, `deleteDocument`, `mergeDocuments`
    - _Requirements: 6.1, 6.7, 6.8_
  - [ ] 4.3 Tạo `DocumentRepositoryImpl.kt`: implement `DocumentRepository`, map giữa `DocumentEntity`/`PageEntity` và domain models, inject `DocumentDao`, `PageDao`, `FileStorageManager`
    - _Requirements: 6.1, 8.1, 8.2_

- [ ] 5. Domain layer — Use Cases
  - [ ] 5.1 Tạo `ManageDocumentUseCase.kt`: `createDocument`, `renameDocument`, `deleteDocument`, `addPage`, `removePage`, `reorderPages`, `mergeDocuments` — mỗi hàm là suspend fun gọi `DocumentRepository`
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_
  - [ ] 5.2 Tạo `ProcessImageUseCase.kt`: orchestrate pipeline xử lý ảnh (perspective transform → sharpen → denoise → apply filter → adjust brightness/contrast), trả về `Bitmap`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
  - [ ] 5.3 Tạo `ExportDocumentUseCase.kt`: `exportToPdf(document, options)` dùng `android.graphics.pdf.PdfDocument`, `exportToImage(page, format)` dùng `Bitmap.compress()`, lưu vào `exports/` qua `FileStorageManager`
    - _Requirements: 7.1, 7.2_

- [ ] 6. Utilities — ImageUtils và PerspectiveTransform
  - [ ] 6.1 Tạo `ImageUtils.kt`: `resizeForDetection(bitmap, maxSize)`, `sharpen(bitmap)` (ConvolutionMatrix 3×3), `denoise(bitmap)` (Gaussian blur nhẹ), `applyFilter(bitmap, FilterType)` (ColorMatrix), `adjustBrightnessContrast(bitmap, brightness, contrast)`, `rotate90(bitmap)`, `createPreviewBitmap(bitmap, maxSize=200)`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 8.5_
  - [ ]* 6.2 Viết property test cho `applyFilter` — Property 7: Áp dụng filter luôn trả về bitmap hợp lệ
    - **Property 7: Áp dụng filter luôn trả về bitmap hợp lệ**
    - **Validates: Requirements 5.3**
  - [ ]* 6.3 Viết property test cho `adjustBrightnessContrast` — Property 8: Image adjustment parameters trong range hợp lệ không crash
    - **Property 8: Image adjustment parameters trong range hợp lệ không crash**
    - **Validates: Requirements 5.4, 5.5**
  - [ ]* 6.4 Viết property test cho `rotate90` — Property 9: Xoay 4 lần 90° trả về ảnh gốc
    - **Property 9: Xoay 4 lần 90° trả về ảnh gốc**
    - **Validates: Requirements 5.6**
  - [ ] 6.5 Tạo `PerspectiveTransform.kt`: `warpPerspective(bitmap, corners: List<PointF>): Bitmap` dùng OpenCV `Imgproc.getPerspectiveTransform` + `Imgproc.warpPerspective`, tính `outputWidth`/`outputHeight` từ khoảng cách các cạnh
    - _Requirements: 4.6_
  - [ ]* 6.6 Viết property test cho `PerspectiveTransform.warpPerspective` — Property 6: Perspective transform tạo ra bitmap hình chữ nhật
    - **Property 6: Perspective transform tạo ra bitmap hình chữ nhật**
    - **Validates: Requirements 4.6**
  - [ ] 6.7 Tạo `BitmapCache.kt`: LRU cache đơn giản cho preview bitmaps, `get(key)`, `put(key, bitmap)`, `clear()`
    - _Requirements: 10.4_

- [ ] 7. Checkpoint — Kiểm tra data layer và utilities
  - Đảm bảo tất cả unit tests và property tests đã viết đều pass. Hỏi người dùng nếu có vấn đề.

- [ ] 8. TFLite và Detection Module
  - [ ] 8.1 Tạo `TFLiteModule.kt` (Hilt): provide `Interpreter` singleton, load từ `assets/models/document_detector.tflite`, cấu hình `numThreads = 2`, `useNNAPI = false`
    - _Requirements: 9.1, 9.2_
  - [ ] 8.2 Tạo `TFLiteDetector.kt` (implement `EdgeDetector`): `preprocessBitmap(bitmap): ByteBuffer` (resize 256×256, normalize [0,1]), `runInference(buffer): Array<Array<FloatArray>>`, `postprocessOutput(output, width, height): List<PointF>`, `detect(bitmap): DetectionResult` với try/catch fallback
    - _Requirements: 9.3, 9.4, 9.5_
  - [ ]* 8.3 Viết property test cho `TFLiteDetector.preprocessBitmap` — Property 21: TFLite preprocessing tạo ByteBuffer đúng shape và range
    - **Property 21: TFLite preprocessing tạo ByteBuffer đúng shape và range**
    - **Validates: Requirements 9.3**
  - [ ]* 8.4 Viết property test cho `TFLiteDetector.postprocessOutput` — Property 22: TFLite postprocessing trả về 4 điểm trong bounds ảnh
    - **Property 22: TFLite postprocessing trả về 4 điểm trong bounds ảnh**
    - **Validates: Requirements 9.4**
  - [ ] 8.5 Tạo `ContourDetector.kt` (implement `EdgeDetector`): dùng OpenCV `Imgproc.Canny` + `Imgproc.findContours`, lọc contour lớn nhất, xấp xỉ thành tứ giác, trả về `DetectionResult(method=CONTOUR)` hoặc `DetectionResult(method=MANUAL)` nếu không tìm được
    - _Requirements: 3.1, 3.5_
  - [ ] 8.6 Tạo `DetectEdgesUseCase.kt`: inject `TFLiteDetector` và `ContourDetector`, implement fallback chain (TFLite → Contour → Manual), chạy trên `Dispatchers.Default`
    - _Requirements: 3.3, 3.5, 9.5, 9.6_
  - [ ]* 8.7 Viết property test cho `DetectEdgesUseCase` — Property 2: Detection trả về 4 điểm góc hợp lệ
    - **Property 2: Detection trả về 4 điểm góc hợp lệ**
    - **Validates: Requirements 3.1, 3.2**
  - [ ]* 8.8 Viết property test cho fallback chain — Property 3: Fallback khi detection thất bại không crash app
    - **Property 3: Fallback khi detection thất bại không crash app**
    - **Validates: Requirements 3.5, 9.5**
  - [ ]* 8.9 Viết property test cho `DetectEdgesUseCase` — Property 5: Detection là deterministic (idempotent)
    - **Property 5: Detection là deterministic (idempotent)**
    - **Validates: Requirements 4.5**

- [ ] 9. Camera Module
  - [ ] 9.1 Tạo `CameraViewModel.kt`: `StateFlow<CameraUiState>` (idle/capturing/captured/error), `capturePhoto(): Uri`, `toggleFlash()`, `setAutoFocus(x, y)`, lưu ảnh tạm vào `FileStorageManager.getTempDir()`
    - _Requirements: 1.1, 1.3, 1.4, 1.5_
  - [ ]* 9.2 Viết property test cho flash toggle — Property 1: Flash toggle là round-trip
    - **Property 1: Flash toggle là round-trip**
    - **Validates: Requirements 1.5**
  - [ ] 9.3 Tạo `CameraScreen.kt` (Jetpack Compose): `PreviewView` qua `AndroidView`, nút chụp, nút flash, guide frame overlay, xử lý permission denied (hiển thị dialog hướng dẫn vào Settings)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.7_
  - [ ] 9.4 Tích hợp CameraX trong `CameraScreen`: khởi tạo `ProcessCameraProvider`, bind `Preview` + `ImageCapture` use case vào `lifecycleOwner`, cấu hình `FocusMeteringAction` cho auto-focus liên tục
    - _Requirements: 1.3, 1.4_

- [ ] 10. Detection Screen
  - [ ] 10.1 Tạo `DetectionViewModel.kt`: nhận `imageUri`, gọi `DetectEdgesUseCase`, emit `DetectionUiState` (loading/success/error), lưu `originalCorners` để reset, expose `currentCorners: StateFlow<List<PointF>>`
    - _Requirements: 3.1, 3.2, 3.4, 3.5, 4.4_
  - [ ] 10.2 Tạo `DetectionScreen.kt`: hiển thị ảnh preview, vẽ 4 drag handle tại các góc (Canvas overlay), loading indicator khi đang detect, nút "Reset" (khôi phục `originalCorners`), nút "Detect lại", nút "Xác nhận"
    - _Requirements: 3.4, 3.6, 4.1, 4.2, 4.3, 4.4, 4.5_
  - [ ]* 10.3 Viết property test cho reset corners — Property 4: Reset corners khôi phục về kết quả detection ban đầu
    - **Property 4: Reset corners khôi phục về kết quả detection ban đầu**
    - **Validates: Requirements 4.4**

- [ ] 11. Edit Module
  - [ ] 11.1 Tạo `EditViewModel.kt`: nhận `pageId` hoặc `imageUri` + `corners`, gọi `ProcessImageUseCase` (preview ở 800px), expose `EditUiState` (previewBitmap, filter, brightness, contrast, rotation), các hàm `applyFilter`, `setBrightness`, `setContrast`, `rotate`, `save`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.8_
  - [ ] 11.2 Tạo `EditScreen.kt`: hiển thị preview ảnh đã transform, 4 nút filter (Original/B&W/Color/Dark), 2 thanh trượt Brightness/Contrast (-100..100), nút xoay 90°, nút "Retake", nút "Lưu" (trigger full quality render + save)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_
  - [ ] 11.3 Implement full quality save trong `EditViewModel.save()`: gọi `ProcessImageUseCase` với ảnh full res, lưu `original`, `processed`, `preview` qua `FileStorageManager`, tạo/cập nhật `Page` trong `DocumentRepository`, hiển thị progress indicator
    - _Requirements: 5.8, 8.1, 8.2, 8.5, 10.5_

- [ ] 12. Checkpoint — Kiểm tra luồng chính Camera → Detection → Edit
  - Đảm bảo luồng Camera → DetectionScreen → EditScreen hoạt động end-to-end qua automated tests. Hỏi người dùng nếu có vấn đề.

- [ ] 13. Document Module
  - [ ] 13.1 Tạo `DocumentViewModel.kt`: inject `ManageDocumentUseCase`, expose `DocumentUiState` (document, pages, isLoading, error), các hàm `reorderPages`, `deletePage`, `renameDocument`, `deleteDocument` (với confirm dialog state)
    - _Requirements: 6.1, 6.3, 6.4, 6.6, 6.9_
  - [ ] 13.2 Tạo `DocumentScreen.kt`: danh sách trang dạng `LazyColumn` với drag-and-drop (dùng `reorderable` library), thumbnail mỗi trang qua Coil, nút xóa trang, nút đổi tên, nút "Thêm trang" (navigate to CameraScreen), nút "Xuất", confirm dialog khi xóa
    - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.9_
  - [ ]* 13.3 Viết property test cho reorder pages — Property 12: Reorder trang bảo toàn tất cả trang
    - **Property 12: Reorder trang bảo toàn tất cả trang**
    - **Validates: Requirements 6.4**
  - [ ]* 13.4 Viết property test cho add page — Property 13: Thêm trang tăng số trang lên đúng 1
    - **Property 13: Thêm trang tăng số trang lên đúng 1**
    - **Validates: Requirements 6.5**
  - [ ]* 13.5 Viết property test cho delete page — Property 14: Xóa trang giảm số trang xuống đúng 1
    - **Property 14: Xóa trang giảm số trang xuống đúng 1**
    - **Validates: Requirements 6.6**
  - [ ]* 13.6 Viết property test cho rename document — Property 11: Đổi tên tài liệu là persistent (round-trip)
    - **Property 11: Đổi tên tài liệu là persistent (round-trip)**
    - **Validates: Requirements 6.3**
  - [ ]* 13.7 Viết property test cho merge documents — Property 15: Merge tài liệu bảo toàn tất cả trang
    - **Property 15: Merge tài liệu bảo toàn tất cả trang**
    - **Validates: Requirements 6.7**

- [ ] 14. Export Module
  - [ ] 14.1 Tạo `ExportViewModel.kt`: inject `ExportDocumentUseCase`, expose `ExportUiState` (idle/exporting/success/error), hàm `exportPdf(documentId, options)`, `exportImage(pageId, format)`, `shareFile(uri)`, `saveToFiles(uri)`, `saveToPhotos(uri)`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  - [ ] 14.2 Implement `ExportDocumentUseCase.exportToPdf`: dùng `android.graphics.pdf.PdfDocument`, mỗi trang ảnh → một trang PDF, lưu vào `exports/`, trả về `Uri` qua `FileProvider`
    - _Requirements: 7.1_
  - [ ] 14.3 Implement `ExportDocumentUseCase.exportToImage`: `Bitmap.compress(JPEG/PNG, quality, outputStream)`, lưu vào `exports/`, trả về `Uri`
    - _Requirements: 7.2_
  - [ ] 14.4 Tạo `ExportScreen.kt`: chọn format (PDF/JPG/PNG), chọn page range (tất cả / trang hiện tại), nút "Chia sẻ" (mở Share Sheet), nút "Lưu vào Files", nút "Lưu vào Photos", progress indicator, snackbar lỗi với nút "Thử lại"
    - _Requirements: 7.3, 7.4, 7.5, 7.6, 10.5_
  - [ ]* 14.5 Viết property test cho export PDF — Property 17: Xuất PDF có đúng số trang
    - **Property 17: Xuất PDF có đúng số trang**
    - **Validates: Requirements 7.1**
  - [ ]* 14.6 Viết property test cho export image — Property 18: Xuất ảnh đúng định dạng được chọn
    - **Property 18: Xuất ảnh đúng định dạng được chọn**
    - **Validates: Requirements 7.2**

- [ ] 15. Home Screen
  - [ ] 15.1 Tạo `HomeViewModel.kt`: inject `ManageDocumentUseCase`, `DocumentRepository`, expose `HomeUiState` (documents: Flow<List<Document>>, isLoading, error), hàm `deleteDocument` (với confirm), `importImages`
    - _Requirements: 6.8, 6.9, 2.1_
  - [ ] 15.2 Tạo `HomeScreen.kt`: `LazyColumn` danh sách tài liệu (thumbnail + tên + ngày sửa), FAB "Scan" trung tâm, nút "Import ảnh", bottom navigation bar (Tài liệu / Scan / Cài đặt), empty state khi chưa có tài liệu, confirm dialog khi xóa
    - _Requirements: 1.1, 2.1, 6.8, 6.9, 10.2_
  - [ ]* 15.3 Viết property test cho document list ordering — Property 16: Danh sách tài liệu được sắp xếp theo updatedAt giảm dần
    - **Property 16: Danh sách tài liệu được sắp xếp theo updatedAt giảm dần**
    - **Validates: Requirements 6.8**
  - [ ]* 15.4 Viết property test cho default document name — Property 10: Tên mặc định tài liệu mới đúng định dạng
    - **Property 10: Tên mặc định tài liệu mới đúng định dạng**
    - **Validates: Requirements 6.2**

- [ ] 16. Navigation — NavGraph và wiring
  - [ ] 16.1 Tạo `NavGraph.kt`: định nghĩa tất cả routes (`home`, `camera/{documentId?}`, `detection/{imageUri}`, `edit/{pageId}`, `document/{documentId}`, `export/{documentId}`, `settings`), truyền arguments giữa các screen
    - _Requirements: 1.1, 2.2, 3.6_
  - [ ] 16.2 Cập nhật `MainActivity.kt`: set up `NavHostController`, gọi `NavGraph`, xử lý deep link từ "Thêm trang" trong DocumentScreen → CameraScreen với `documentId`
    - _Requirements: 1.6_
  - [ ] 16.3 Kết nối tất cả navigation actions: CameraScreen → DetectionScreen (sau chụp), DetectionScreen → EditScreen (sau confirm), EditScreen → DocumentScreen (sau save), DocumentScreen → ExportScreen, HomeScreen → DocumentScreen (tap existing doc)
    - _Requirements: 1.4, 3.6, 4.6, 6.1_

- [ ] 17. Checkpoint — Kiểm tra toàn bộ luồng chính
  - Đảm bảo luồng đầy đủ Camera → Detection → Edit → Document → Export hoạt động. Hỏi người dùng nếu có vấn đề.

- [ ] 18. Settings Module
  - [ ] 18.1 Tạo `SettingsViewModel.kt`: dùng `SharedPreferences`, expose `SettingsUiState` (defaultExportFormat, defaultFilter, storageUsedBytes), hàm `setDefaultExportFormat`, `setDefaultFilter`, `deleteOriginals`, `calculateStorageUsage` (đọc từ `FileStorageManager.calculateTotalSize()`)
    - _Requirements: 11.1, 11.2, 11.3, 11.4_
  - [ ] 18.2 Tạo `SettingsScreen.kt`: dropdown chọn format xuất mặc định, dropdown chọn filter mặc định, hiển thị dung lượng đang dùng (format MB/GB), nút "Xóa ảnh gốc" với confirm dialog
    - _Requirements: 11.1, 11.2, 11.3, 11.4_
  - [ ]* 18.3 Viết property test cho settings round-trip — Property 23: Settings round-trip bảo toàn giá trị
    - **Property 23: Settings round-trip bảo toàn giá trị**
    - **Validates: Requirements 11.1, 11.2**
  - [ ]* 18.4 Viết property test cho storage reporting — Property 24: Báo cáo dung lượng bằng tổng kích thước file
    - **Property 24: Báo cáo dung lượng bằng tổng kích thước file**
    - **Validates: Requirements 11.3**
  - [ ]* 18.5 Viết property test cho delete originals — Property 25: Xóa ảnh gốc không ảnh hưởng đến ảnh processed
    - **Property 25: Xóa ảnh gốc không ảnh hưởng đến ảnh processed**
    - **Validates: Requirements 11.4**

- [ ] 19. Storage và Data integrity tests
  - [ ]* 19.1 Viết property test cho storage round-trip — Property 19: Storage round-trip bảo toàn dữ liệu
    - **Property 19: Storage round-trip bảo toàn dữ liệu**
    - **Validates: Requirements 8.1, 8.2**
  - [ ]* 19.2 Viết property test cho preview size — Property 20: Preview nhỏ hơn ảnh processed
    - **Property 20: Preview nhỏ hơn ảnh processed**
    - **Validates: Requirements 8.5**
  - [ ]* 19.3 Viết unit tests tích hợp Room: CRUD `DocumentDao` và `PageDao` với `in-memory database`, kiểm tra cascade delete, kiểm tra Flow emit khi data thay đổi
    - _Requirements: 8.1, 8.3_
  - [ ]* 19.4 Viết unit tests cho `FileStorageManager`: tạo/xóa file, tính dung lượng, kiểm tra thư mục tồn tại
    - _Requirements: 8.1, 8.5_

- [ ] 20. Final checkpoint — Đảm bảo tất cả tests pass
  - Chạy toàn bộ test suite, đảm bảo coverage đạt target (Domain ≥ 90%, Data ≥ 80%, ViewModel ≥ 70%). Hỏi người dùng nếu có vấn đề.

---

## Ghi chú

- Tasks đánh dấu `*` là optional, có thể bỏ qua để MVP nhanh hơn
- Mỗi task tham chiếu requirements cụ thể để đảm bảo traceability
- Property tests dùng Kotest `checkAll(iterations = 100, ...)` theo design Section 12.2
- Bitmap lớn (>4MB) luôn xử lý trên `Dispatchers.Default`, không giữ reference trên Main thread
- Không giữ Bitmap trong ViewModel — chỉ giữ URI/path
- TFLite Interpreter là Singleton, load 1 lần khi app start
