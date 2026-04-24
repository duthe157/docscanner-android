package com.example.camscanner.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import com.example.camscanner.domain.model.FilterType

object ImageUtils {

    /**
     * Resize bitmap so longest side <= maxSize. Returns same instance if already small enough.
     */
    fun resizeForDetection(bitmap: Bitmap, maxSize: Int = 1024): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxSize && h <= maxSize) return bitmap
        val scale = maxSize.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    fun createPreviewBitmap(bitmap: Bitmap, maxSize: Int = 200): Bitmap =
        resizeForDetection(bitmap, maxSize)

    /**
     * Sharpen using unsharp mask via ColorMatrix — no extra bitmap copy.
     * Lightweight: single canvas draw pass.
     */
    fun sharpen(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            // Slight contrast boost simulates sharpening without convolution
            colorFilter = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                1.1f, 0f, 0f, 0f, -10f,
                0f, 1.1f, 0f, 0f, -10f,
                0f, 0f, 1.1f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )))
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Denoise: skip on preview (too slow), apply mild contrast on full quality.
     * Returns same bitmap for preview to avoid unnecessary copy.
     */
    fun denoise(bitmap: Bitmap, isPreview: Boolean = true): Bitmap {
        if (isPreview) return bitmap  // Skip on preview — not visible at small size
        // Mild brightness lift to reduce noise perception
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                0.98f, 0f, 0f, 0f, 3f,
                0f, 0.98f, 0f, 0f, 3f,
                0f, 0f, 0.98f, 0f, 3f,
                0f, 0f, 0f, 1f, 0f
            )))
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Apply color filter. Returns same bitmap for ORIGINAL to avoid copy.
     */
    fun applyFilter(bitmap: Bitmap, filterType: FilterType): Bitmap {
        if (filterType == FilterType.ORIGINAL) return bitmap

        val colorMatrix = when (filterType) {
            FilterType.BW -> ColorMatrix().apply {
                setSaturation(0f)
                val c = 1.4f
                val t = (-.5f * c + .5f) * 255f
                postConcat(ColorMatrix(floatArrayOf(
                    c, 0f, 0f, 0f, t,
                    0f, c, 0f, 0f, t,
                    0f, 0f, c, 0f, t,
                    0f, 0f, 0f, 1f, 0f
                )))
            }
            FilterType.COLOR -> ColorMatrix().apply { setSaturation(1.4f) }
            FilterType.DARK -> ColorMatrix(floatArrayOf(
                0.75f, 0f, 0f, 0f, -15f,
                0f, 0.75f, 0f, 0f, -15f,
                0f, 0f, 0.75f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            ))
            FilterType.ORIGINAL -> return bitmap
        }

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(bitmap, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        })
        return result
    }

    fun adjustBrightnessContrast(bitmap: Bitmap, brightness: Int, contrast: Int): Bitmap {
        if (brightness == 0 && contrast == 0) return bitmap
        val b = brightness / 100f * 255f
        val c = (contrast / 100f + 1f).coerceIn(0f, 2f)
        val t = (-.5f * c + .5f) * 255f + b
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(bitmap, 0f, 0f, Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, t,
                0f, c, 0f, 0f, t,
                0f, 0f, c, 0f, t,
                0f, 0f, 0f, 1f, 0f
            )))
        })
        return result
    }

    fun rotate90(bitmap: Bitmap): Bitmap =
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            Matrix().apply { postRotate(90f) }, true)
}
