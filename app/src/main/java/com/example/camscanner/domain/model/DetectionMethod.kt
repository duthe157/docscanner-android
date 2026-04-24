package com.example.camscanner.domain.model

enum class DetectionMethod {
    TFLITE,   // TensorFlow Lite model
    CONTOUR,  // OpenCV contour detection
    MANUAL    // User manual adjustment
}
