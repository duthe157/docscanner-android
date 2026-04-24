# OpenCV Android Setup Guide

## Trạng thái hiện tại: ✅ ĐÃ TÍCH HỢP

OpenCV 4.8.0 đã được tích hợp thành công vào project theo cách sau.

---

## Cách tích hợp đã dùng (Module approach)

### Cấu trúc file

```
app/src/main/jniLibs/
├── arm64-v8a/libopencv_java4.so    (19 MB)
├── armeabi-v7a/libopencv_java4.so  (11.7 MB)
├── x86/libopencv_java4.so          (37.5 MB)
└── x86_64/libopencv_java4.so       (24.8 MB)
```

OpenCV SDK được link như một Gradle module từ đường dẫn local trong `settings.gradle.kts`.

---

## Nếu cần setup lại trên máy khác

### Bước 1: Tải OpenCV Android SDK

Tải `opencv-4.8.0-android-sdk.zip` từ:
- https://github.com/opencv/opencv/releases/tag/4.8.0
- Hoặc https://sourceforge.net/projects/opencvlibrary/files/4.8.0/

Giải nén ra, ví dụ: `C:\Users\<USER>\Downloads\opencv-4.8.0-android-sdk\`

### Bước 2: Copy .so files

```
Từ: OpenCV-android-sdk/sdk/native/libs/<ABI>/libopencv_java4.so
Đến: app/src/main/jniLibs/<ABI>/libopencv_java4.so
```

Copy đủ 4 ABI: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

### Bước 3: Patch OpenCV SDK build.gradle

File: `<opencv-sdk>/OpenCV-android-sdk/sdk/build.gradle`

Thay toàn bộ nội dung bằng:

```groovy
apply plugin: 'com.android.library'

android {
    namespace "org.opencv"
    compileSdk 34

    defaultConfig {
        minSdk 21
        targetSdk 34
    }

    buildFeatures {
        buildConfig true
        aidl true
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    sourceSets {
        main {
            jniLibs.srcDirs = ['native/libs']
            java.srcDirs = ['java/src']
            aidl.srcDirs = ['java/src']
            res.srcDirs = ['java/res']
            manifest.srcFile 'java/AndroidManifest.xml'
        }
    }
}

dependencies {
}
```

### Bước 4: Cập nhật settings.gradle.kts

```kotlin
// Thêm vào cuối file settings.gradle.kts
val opencvSdk = "C:\\Users\\<USER>\\Downloads\\opencv-4.8.0-android-sdk\\OpenCV-android-sdk"
include(":opencv")
project(":opencv").projectDir = File("$opencvSdk/sdk")
```

Thay `<USER>` bằng username của bạn.

### Bước 5: Cập nhật app/build.gradle.kts

```kotlin
dependencies {
    // ...
    implementation(project(":opencv"))
}
```

### Bước 6: Build

```bash
.\gradlew.bat assembleDebug --no-daemon
```

---

## Lưu ý quan trọng

- OpenCV SDK 4.8.0 dùng AGP cũ (Groovy DSL) — **không tương thích trực tiếp** với AGP 8.x
- Phải patch `build.gradle` của SDK (bước 3) để bỏ CMake build và fix namespace
- `.so` files đã pre-built sẵn trong SDK, không cần compile lại từ C++ source
- `System.loadLibrary("opencv_java4")` được gọi trong `CamScannerApplication.onCreate()`

---

## Tính năng OpenCV đã enable

| Tính năng | Class | Trạng thái |
|-----------|-------|-----------|
| Perspective Transform | `PerspectiveTransform.kt` | ✅ Active |
| Contour Detection (Canny) | `ContourDetector.kt` | ✅ Active |
| Gaussian Blur | `ImageUtils.kt` | ✅ Active |
| Color conversion | `ImageUtils.kt` | ✅ Active |
