package com.example.camscanner.domain.usecase

import android.graphics.Bitmap
import com.example.camscanner.data.ml.ContourDetector
import com.example.camscanner.data.ml.TFLiteDetector
import com.example.camscanner.domain.model.DetectionMethod
import com.example.camscanner.domain.model.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DetectEdgesUseCase @Inject constructor(
    private val tfliteDetector: TFLiteDetector,
    private val contourDetector: ContourDetector
) {
    suspend fun execute(bitmap: Bitmap): DetectionResult = withContext(Dispatchers.Default) {
        // Try TFLite first
        val tfliteResult = try {
            tfliteDetector.detect(bitmap)
        } catch (e: Exception) {
            null
        }

        // If TFLite succeeded with good confidence, return it
        if (tfliteResult != null && tfliteResult.method == DetectionMethod.TFLITE) {
            return@withContext tfliteResult
        }

        // Fallback to Contour detection
        val contourResult = try {
            contourDetector.detect(bitmap)
        } catch (e: Exception) {
            null
        }

        // If Contour succeeded, return it
        if (contourResult != null && contourResult.method == DetectionMethod.CONTOUR) {
            return@withContext contourResult
        }

        // Final fallback to manual
        tfliteResult ?: contourResult ?: DetectionResult(
            corners = emptyList(),
            confidence = 0f,
            method = DetectionMethod.MANUAL
        )
    }
}
