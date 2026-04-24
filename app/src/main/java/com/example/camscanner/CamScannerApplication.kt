package com.example.camscanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CamScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // OpenCV native library loaded automatically from jniLibs
        System.loadLibrary("opencv_java4")
    }
}
