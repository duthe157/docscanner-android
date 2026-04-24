# Script chạy app Android từ command line
Write-Host "=== CamScanner App Builder ===" -ForegroundColor Cyan

# 1. Kiểm tra emulator đang chạy chưa
Write-Host "`n[1/5] Kiểm tra emulator..." -ForegroundColor Yellow
$devices = & "$env:ANDROID_HOME\platform-tools\adb.exe" devices | Select-String "emulator"

if (-not $devices) {
    Write-Host "Khởi động emulator Pixel_7..." -ForegroundColor Green
    Start-Process -FilePath "$env:ANDROID_HOME\emulator\emulator.exe" -ArgumentList "-avd", "Pixel_7" -WindowStyle Normal
    
    Write-Host "Đợi emulator khởi động (30 giây)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Đợi boot xong
    $bootComplete = $false
    $maxWait = 60
    $waited = 0
    while (-not $bootComplete -and $waited -lt $maxWait) {
        $bootStatus = & "$env:ANDROID_HOME\platform-tools\adb.exe" shell getprop sys.boot_completed 2>$null
        if ($bootStatus -match "1") {
            $bootComplete = $true
            Write-Host "Emulator đã sẵn sàng!" -ForegroundColor Green
        } else {
            Write-Host "." -NoNewline
            Start-Sleep -Seconds 2
            $waited += 2
        }
    }
} else {
    Write-Host "Emulator đã chạy!" -ForegroundColor Green
}

# 2. Build APK
Write-Host "`n[2/5] Build APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build thất bại!" -ForegroundColor Red
    exit 1
}

Write-Host "Build thành công!" -ForegroundColor Green

# 3. Install APK
Write-Host "`n[3/5] Cài đặt APK lên emulator..." -ForegroundColor Yellow
.\gradlew.bat installDebug

# 4. Launch app
Write-Host "`n[4/5] Khởi động app..." -ForegroundColor Yellow
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.example.camscanner/.MainActivity

# 5. Show logs
Write-Host "`n[5/5] Hiển thị logs (Ctrl+C để thoát)..." -ForegroundColor Yellow
Write-Host "App đang chạy trên emulator!" -ForegroundColor Green
& "$env:ANDROID_HOME\platform-tools\adb.exe" logcat -s "CamScanner:*" "*:E"
