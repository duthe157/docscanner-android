package com.example.camscanner.domain.model

import android.graphics.PointF

data class DetectionResult(
    val corners: List<PointF>,  // 4 corners: topLeft, topRight, bottomRight, bottomLeft
    val confidence: Float,
    val method: DetectionMethod
)
