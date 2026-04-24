package com.example.camscanner.domain.model

data class ExportOptions(
    val format: ExportFormat,
    val quality: Int = 90,
    val pageRange: IntRange? = null
)
