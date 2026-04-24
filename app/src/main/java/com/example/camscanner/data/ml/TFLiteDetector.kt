package com.example.camscanner.data.ml

import android.graphics.Bitmap
import android.graphics.PointF
import com.example.camscanner.domain.model.DetectionMethod
import com.example.camscanner.domain.model.DetectionResult
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * YOLO Pose TFLite detector for document corner detection.
 *
 * Model specs (document_detector.tflite):
 *   Input:  [1, 416, 416, 3]  float32  (RGB, normalized 0-1)
 *   Output: [1, 17, 3549]     float32
 *     - 3549 anchor predictions
 *     - 17 values per anchor:
 *       [0]   cx (center x, normalized)
 *       [1]   cy (center y, normalized)
 *       [2]   w  (width, normalized)
 *       [3]   h  (height, normalized)
 *       [4]   obj_conf (objectness score)
 *       [5..7]  keypoint 0: x, y, conf  (top-left)
 *       [8..10] keypoint 1: x, y, conf  (top-right)
 *       [11..13] keypoint 2: x, y, conf (bottom-right)
 *       [14..16] keypoint 3: x, y, conf (bottom-left)
 */
@Singleton
class TFLiteDetector @Inject constructor(
    private val interpreter: Interpreter?
) : EdgeDetector {

    companion object {
        private const val INPUT_SIZE = 416
        private const val CONF_THRESHOLD = 0.25f
        private const val NUM_KEYPOINTS = 4
    }

    override suspend fun detect(bitmap: Bitmap): DetectionResult {
        if (interpreter == null) {
            return fallback(bitmap.width, bitmap.height)
        }

        return try {
            val inputBuffer = preprocessBitmap(bitmap)

            // Output shape: [1, 17, 3549]
            val rawOutput = Array(1) { Array(17) { FloatArray(3549) } }
            interpreter.run(inputBuffer, rawOutput)

            val corners = parseYoloPoseOutput(rawOutput[0], bitmap.width, bitmap.height)

            if (corners != null && isValidQuad(corners, bitmap.width, bitmap.height)) {
                DetectionResult(
                    corners = corners,
                    confidence = 0.85f,
                    method = DetectionMethod.TFLITE
                )
            } else {
                fallback(bitmap.width, bitmap.height)
            }
        } catch (e: Exception) {
            fallback(bitmap.width, bitmap.height)
        }
    }

    /**
     * Parse YOLO Pose output [17, 3549]:
     * - Transpose to [3549, 17] for easier iteration
     * - Filter by objectness confidence
     * - Pick best anchor
     * - Extract 4 keypoints
     */
    private fun parseYoloPoseOutput(
        output: Array<FloatArray>,  // [17, 3549]
        imageWidth: Int,
        imageHeight: Int
    ): List<PointF>? {
        val numAnchors = output[0].size  // 3549
        val numValues = output.size      // 17

        var bestConf = CONF_THRESHOLD
        var bestAnchorIdx = -1

        // Find anchor with highest objectness score (index 4)
        for (i in 0 until numAnchors) {
            val conf = output[4][i]  // objectness at row 4
            if (conf > bestConf) {
                bestConf = conf
                bestAnchorIdx = i
            }
        }

        if (bestAnchorIdx < 0) return null

        // Extract 4 keypoints from best anchor
        // Keypoint layout: rows 5-16, groups of 3 (x, y, conf)
        val keypoints = mutableListOf<PointF>()
        for (k in 0 until NUM_KEYPOINTS) {
            val baseRow = 5 + k * 3
            val kx = output[baseRow][bestAnchorIdx]
            val ky = output[baseRow + 1][bestAnchorIdx]
            // kconf = output[baseRow + 2][bestAnchorIdx]  // keypoint confidence

            // Coordinates are normalized 0-1 relative to input size (416)
            keypoints.add(PointF(kx * imageWidth, ky * imageHeight))
        }

        return if (keypoints.size == 4) orderCorners(keypoints) else null
    }

    /**
     * Order corners: topLeft, topRight, bottomRight, bottomLeft
     * Using sum/diff method (same as Python reference).
     */
    private fun orderCorners(pts: List<PointF>): List<PointF> {
        val topLeft     = pts.minByOrNull { it.x + it.y }!!
        val bottomRight = pts.maxByOrNull { it.x + it.y }!!
        val topRight    = pts.minByOrNull { it.y - it.x }!!
        val bottomLeft  = pts.maxByOrNull { it.y - it.x }!!
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun isValidQuad(corners: List<PointF>, width: Int, height: Int): Boolean {
        if (corners.size != 4) return false
        val margin = -50f
        if (corners.any { it.x < margin || it.x > width - margin || it.y < margin || it.y > height - margin }) {
            return false
        }
        // Minimum area check
        val area = shoelaceArea(corners)
        return area > width * height * 0.05f
    }

    private fun shoelaceArea(pts: List<PointF>): Float {
        var area = 0f
        for (i in pts.indices) {
            val j = (i + 1) % pts.size
            area += pts[i].x * pts[j].y
            area -= pts[j].x * pts[i].y
        }
        return kotlin.math.abs(area / 2f)
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        // [1, 416, 416, 3] float32
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)  // R
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)   // G
            buffer.putFloat((pixel and 0xFF) / 255f)            // B
        }

        buffer.rewind()
        return buffer
    }

    private fun fallback(width: Int, height: Int): DetectionResult {
        val mx = width * 0.08f
        val my = height * 0.08f
        return DetectionResult(
            corners = listOf(
                PointF(mx, my),
                PointF(width - mx, my),
                PointF(width - mx, height - my),
                PointF(mx, height - my)
            ),
            confidence = 0f,
            method = DetectionMethod.MANUAL
        )
    }
}
