package com.example.camscanner.data.ml

import android.graphics.Bitmap
import android.graphics.PointF
import com.example.camscanner.domain.model.DetectionMethod
import com.example.camscanner.domain.model.DetectionResult
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class ContourDetector @Inject constructor() : EdgeDetector {

    override suspend fun detect(bitmap: Bitmap): DetectionResult {
        return try {
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            val gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)

            // Stronger blur to reduce noise before edge detection
            Imgproc.GaussianBlur(gray, gray, Size(9.0, 9.0), 0.0)

            // Dilate slightly to close gaps in document edges
            val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
            val dilated = Mat()
            Imgproc.dilate(gray, dilated, kernel)

            val edges = Mat()
            // Lower thresholds to catch more edges
            Imgproc.Canny(dilated, edges, 30.0, 100.0)

            // Dilate edges to connect broken lines
            Imgproc.dilate(edges, edges, kernel)

            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                edges, contours, hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            val imageArea = bitmap.width.toDouble() * bitmap.height.toDouble()
            var bestResult: DetectionResult? = null

            // Try contours from largest to smallest
            val sortedContours = contours.sortedByDescending { Imgproc.contourArea(it) }

            for (contour in sortedContours.take(5)) {
                val area = Imgproc.contourArea(contour)
                if (area < imageArea * 0.05) break  // Too small

                val contour2f = MatOfPoint2f(*contour.toArray())
                val perimeter = Imgproc.arcLength(contour2f, true)

                // Try different epsilon values to find a 4-point approximation
                for (epsilonFactor in listOf(0.02, 0.03, 0.04, 0.05, 0.06)) {
                    val approx = MatOfPoint2f()
                    Imgproc.approxPolyDP(contour2f, approx, epsilonFactor * perimeter, true)

                    if (approx.rows() == 4) {
                        val rawCorners = approx.toArray().map { PointF(it.x.toFloat(), it.y.toFloat()) }
                        val ordered = orderCorners(rawCorners)

                        // Validate: corners should form a reasonable quadrilateral
                        if (isValidQuad(ordered, bitmap.width, bitmap.height)) {
                            bestResult = DetectionResult(
                                corners = ordered,
                                confidence = (area / imageArea).toFloat().coerceAtMost(0.95f),
                                method = DetectionMethod.CONTOUR
                            )
                            break
                        }
                    }
                }
                if (bestResult != null) break
            }

            mat.release(); gray.release(); dilated.release()
            edges.release(); hierarchy.release(); kernel.release()

            bestResult ?: DetectionResult(
                corners = getDefaultCorners(bitmap.width, bitmap.height),
                confidence = 0.0f,
                method = DetectionMethod.MANUAL
            )
        } catch (e: Exception) {
            DetectionResult(
                corners = getDefaultCorners(bitmap.width, bitmap.height),
                confidence = 0.0f,
                method = DetectionMethod.MANUAL
            )
        }
    }

    /**
     * Order corners as: topLeft, topRight, bottomRight, bottomLeft
     * Uses sum/diff of coordinates — robust method used in most scanner apps.
     */
    private fun orderCorners(pts: List<PointF>): List<PointF> {
        // Top-left: smallest sum (x+y)
        // Bottom-right: largest sum (x+y)
        // Top-right: smallest diff (y-x)
        // Bottom-left: largest diff (y-x)
        val topLeft     = pts.minByOrNull { it.x + it.y }!!
        val bottomRight = pts.maxByOrNull { it.x + it.y }!!
        val topRight    = pts.minByOrNull { it.y - it.x }!!
        val bottomLeft  = pts.maxByOrNull { it.y - it.x }!!
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun isValidQuad(corners: List<PointF>, width: Int, height: Int): Boolean {
        if (corners.size != 4) return false
        // All points must be within image bounds (with small margin)
        val margin = -20f
        if (corners.any { it.x < margin || it.x > width - margin || it.y < margin || it.y > height - margin }) {
            return false
        }
        // Minimum side length — avoid degenerate quads
        val minSide = minOf(width, height) * 0.1f
        for (i in 0..3) {
            val a = corners[i]
            val b = corners[(i + 1) % 4]
            val dx = b.x - a.x; val dy = b.y - a.y
            if (sqrt(dx * dx + dy * dy) < minSide) return false
        }
        return true
    }

    private fun getDefaultCorners(width: Int, height: Int): List<PointF> {
        val mx = width * 0.08f
        val my = height * 0.08f
        return listOf(
            PointF(mx, my),
            PointF(width - mx, my),
            PointF(width - mx, height - my),
            PointF(mx, height - my)
        )
    }
}
