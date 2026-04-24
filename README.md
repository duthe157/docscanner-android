# CamScanner App - Android Native

Ứng dụng scanner tài liệu offline, xây dựng bằng Kotlin + Jetpack Compose theo kiến trúc MVVM + Clean Architecture.

## 📋 Tổng quan

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Kiến trúc**: MVVM + Clean Architecture
- **Database**: Room
- **DI**: Hilt
- **Camera**: CameraX
- **ML**: TensorFlow Lite + OpenCV (optional)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## 🎯 Tính năng hiện tại

✅ **Đã triển khai:**
- Camera với flash toggle và permission handling
- Edge detection cơ bản
- Image editing (filter, brightness, contrast, rotation)
- Navigation flow: Home → Camera → Detection → Edit
- File storage management
- Room database setup

🚧 **Đang phát triển:**
- Document management (CRUD operations)
- PDF/Image export
- Settings screen
- TFLite model integration
- OpenCV perspective transform

## 🛠️ Yêu cầu môi trường

### Bắt buộc:
1. **JDK 17** - Đã cấu hình trong `gradle.properties`
   - Đường dẫn hiện tại: `C:\javaLibs\jdk-17.0.12`
   - Nếu JDK 17 ở vị trí khác, cập nhật `gradle.properties`:
     ```properties
     org.gradle.java.home=<ĐƯỜNG_DẪN_JDK_17>
     ```

2. **Android SDK** (API 26-34)
   - SDK Platform 34 (Android 14) - Required
   - SDK Build-Tools 34.0.0+
   - Android SDK Platform-Tools
   - Android Emulator

3. **Gradle 8.2** (đã có trong wrapper)

### Tùy chọn:
- **Android Studio** Hedgehog 2023.1.1+ (để phát triển với UI)
- **OpenCV Android SDK 4.8.0** (xem `SETUP_OPENCV.md`)

## 📦 Cài đặt môi trường

### Bước 1: Kiểm tra JDK 17

```bash
# Kiểm tra Java version
java -version
# Nên hiển thị: java version "17.x.x"

# Nếu chưa có JDK 17, tải tại:
# https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
```

### Bước 2: Cài Android SDK

**Cách 1: Qua Android Studio**
1. Mở Android Studio → **Tools** → **SDK Manager**
2. **SDK Platforms**: Chọn Android 14.0 (API 34)
3. **SDK Tools**: Đảm bảo có:
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools
   - Android Emulator

**Cách 2: Command line tools**
```bash
# Tải Android Command Line Tools từ:
# https://developer.android.com/studio#command-tools

# Cài SDK Platform 34
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
```

### Bước 3: Tạo máy ảo (AVD)

**Qua Android Studio:**
1. **Tools** → **Device Manager**
2. Click **Create Device**
3. Chọn **Pixel 7** (hoặc device bất kỳ)
4. Chọn **System Image**: API 33 (Android 13) hoặc API 34
5. Click **Finish**

**Qua command line:**
```bash
# Liệt kê AVD có sẵn
emulator -list-avds

# Tạo AVD mới
avdmanager create avd -n Pixel_7 -k "system-images;android-33;google_apis;x86_64" -d pixel_7

# Khởi động emulator
emulator -avd Pixel_7
```

### Bước 4: Đặt TFLite model (tùy chọn)

Copy file `document_detector.tflite` vào:
```
app/src/main/assets/models/document_detector.tflite
```

Nếu chưa có model, file placeholder đã được tạo sẵn (`.gitkeep`).

## 🚀 Build và chạy

### Phương pháp 1: Từ Kiro workspace (Khuyến nghị)

```bash
# 1. Build APK
.\gradlew.bat assembleDebug --no-daemon

# 2. Kiểm tra emulator đang chạy
adb devices

# 3. Cài đặt APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Khởi động app
adb shell am start -n com.example.camscanner/.MainActivity
```

**Lưu ý:** Build lần đầu có thể mất 2-3 phút do tải dependencies.

### Phương pháp 2: Từ Android Studio

1. Mở project trong Android Studio
2. Đợi Gradle sync hoàn tất
3. Chọn device/emulator từ dropdown
4. Click **Run** (Shift+F10) hoặc nút ▶️

### Phương pháp 3: Build release APK

```bash
# Build release APK (đã minify)
.\gradlew.bat assembleRelease --no-daemon

# APK output:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

## 🏗️ Cấu trúc project

```
app/src/main/
├── java/com/example/camscanner/
│   ├── di/                          # Dependency Injection (Hilt modules)
│   │   ├── AppModule.kt            # FileStorageManager, StorageRepository
│   │   ├── DatabaseModule.kt       # Room Database, DAOs
│   │   └── TFLiteModule.kt         # TFLite Interpreter
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── db/                 # Room Database
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── DocumentDao.kt
│   │   │   │   └── PageDao.kt
│   │   │   ├── entity/             # Database Entities
│   │   │   │   ├── DocumentEntity.kt
│   │   │   │   └── PageEntity.kt
│   │   │   └── FileStorageManager.kt
│   │   └── ml/                     # Machine Learning
│   │       ├── EdgeDetector.kt     # Interface
│   │       ├── TFLiteDetector.kt   # TFLite implementation
│   │       └── ContourDetector.kt  # OpenCV implementation
│   │
│   ├── domain/
│   │   ├── model/                  # Domain models
│   │   │   ├── Document.kt
│   │   │   ├── Page.kt
│   │   │   ├── DetectionResult.kt
│   │   │   ├── FilterType.kt
│   │   │   └── ExportOptions.kt
│   │   ├── repository/             # Repository interfaces
│   │   │   └── DocumentRepository.kt
│   │   └── usecase/                # Business logic
│   │       ├── DetectEdgesUseCase.kt
│   │       └── ProcessImageUseCase.kt
│   │
│   ├── presentation/               # UI Layer (Jetpack Compose)
│   │   ├── navigation/
│   │   │   └── NavGraph.kt        # Navigation routes
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── camera/
│   │   │   ├── CameraScreen.kt
│   │   │   └── CameraViewModel.kt
│   │   ├── detection/
│   │   │   ├── DetectionScreen.kt
│   │   │   └── DetectionViewModel.kt
│   │   └── edit/
│   │       ├── EditScreen.kt
│   │       └── EditViewModel.kt
│   │
│   ├── util/                       # Utilities
│   │   ├── ImageUtils.kt          # Image processing
│   │   ├── PerspectiveTransform.kt # OpenCV transforms
│   │   └── BitmapCache.kt         # LRU cache
│   │
│   ├── MainActivity.kt
│   ├── CamScannerApp.kt
│   └── CamScannerApplication.kt
│
├── res/                            # Resources
│   ├── values/
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── xml/
│       └── file_paths.xml         # FileProvider config
│
├── assets/
│   └── models/
│       └── .gitkeep               # Placeholder for TFLite model
│
└── AndroidManifest.xml
```

## 🔧 Cấu hình quan trọng

### gradle.properties
```properties
# JDK 17 path (cập nhật nếu cần)
org.gradle.java.home=C:\\javaLibs\\jdk-17.0.12

# Memory settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

### app/build.gradle.kts
```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        minSdk = 26
        targetSdk = 34
        
        // ABI filters (giảm kích thước APK)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }
}
```

## 🧪 Testing

```bash
# Run unit tests
.\gradlew.bat test

# Run instrumented tests (cần emulator/device)
.\gradlew.bat connectedAndroidTest

# Generate test coverage report
.\gradlew.bat jacocoTestReport
```

## 📱 Luồng ứng dụng hiện tại

```
HomeScreen (Empty state)
    ↓ [Tap FAB]
CameraScreen (CameraX + Flash toggle)
    ↓ [Capture photo]
DetectionScreen (Edge detection)
    ↓ [Confirm corners]
EditScreen (Filter + Brightness + Contrast + Rotation)
    ↓ [Save]
HomeScreen (Document list - TODO)
```

## 🐛 Troubleshooting

### 1. Lỗi "SDK location not found"
Tạo file `local.properties` ở root project:
```properties
sdk.dir=C:\\Users\\<USERNAME>\\AppData\\Local\\Android\\Sdk
```

### 2. Lỗi jlink với JDK 21
Đảm bảo `gradle.properties` đang dùng JDK 17:
```properties
org.gradle.java.home=C:\\javaLibs\\jdk-17.0.12
```

### 3. Build failed - Dependencies
```bash
# Clean và rebuild
.\gradlew.bat clean
.\gradlew.bat assembleDebug --refresh-dependencies
```

### 4. Emulator không khởi động
- Bật **Virtualization** trong BIOS (Intel VT-x / AMD-V)
- Cài **Intel HAXM** (cho Intel CPU):
  ```bash
  sdkmanager "extras;intel;Hardware_Accelerated_Execution_Manager"
  ```

### 5. App crash khi mở camera
Kiểm tra permissions trong `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### 6. Lỗi "Execution failed for task ':app:compileDebugKotlin'"
```bash
# Xóa build cache
.\gradlew.bat clean
rm -rf .gradle
rm -rf app/build

# Rebuild
.\gradlew.bat assembleDebug
```

## 📚 Tài liệu bổ sung

- **Spec files**: `.kiro/specs/cam-scanner-app/`
  - `requirements.md` - Yêu cầu chức năng
  - `design.md` - Thiết kế kiến trúc
  - `tasks.md` - Kế hoạch triển khai
- **OpenCV setup**: `SETUP_OPENCV.md`

## 🔄 Workflow phát triển

1. **Tạo feature branch**
   ```bash
   git checkout -b feature/ten-tinh-nang
   ```

2. **Phát triển theo tasks.md**
   - Mỗi task tham chiếu requirements cụ thể
   - Viết tests trước khi implement (TDD)

3. **Build và test**
   ```bash
   .\gradlew.bat assembleDebug
   .\gradlew.bat test
   ```

4. **Cài đặt và test thủ công**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.example.camscanner/.MainActivity
   ```

5. **Cập nhật documentation**
   - Cập nhật README.md nếu có thay đổi build process
   - Cập nhật design.md nếu có thay đổi kiến trúc
   - Đánh dấu task hoàn thành trong tasks.md

## 📝 Changelog

### [Unreleased]
- ✅ Setup project structure với Hilt + Room + CameraX
- ✅ Implement Camera screen với flash toggle
- ✅ Implement Detection screen (basic)
- ✅ Implement Edit screen với filters và adjustments
- ✅ Setup Navigation với NavGraph
- 🚧 Document management (in progress)
- 🚧 PDF/Image export (in progress)

## 📄 License

[Thêm license của bạn ở đây]

## 👥 Contributors

[Thêm thông tin contributors]

---

**Lưu ý:** Đây là project đang trong quá trình phát triển. Một số tính năng chưa hoàn thiện. Xem `tasks.md` để biết tiến độ chi tiết.
