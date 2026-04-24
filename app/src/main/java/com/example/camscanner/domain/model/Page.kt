package com.example.camscanner.domain.model

import android.graphics.PointF

data class Page(
    val id: String,
    val documentId: String,
    val order: Int,
    val originalPath: String,
    val processedPath: String,
    val previewPath: String,
    val filter: FilterType,
    val brightness: Int,      // -100..100
    val contrast: Int,        // -100..100
    val rotation: Int,        // 0, 90, 180, 270
    val corners: List<PointF> // 4 corners confirmed by user
)
