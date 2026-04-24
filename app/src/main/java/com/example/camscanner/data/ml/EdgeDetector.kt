package com.example.camscanner.data.ml

import android.graphics.Bitmap
import com.example.camscanner.domain.model.DetectionResult

interface EdgeDetector {
    suspend fun detect(bitmap: Bitmap): DetectionResult
}
