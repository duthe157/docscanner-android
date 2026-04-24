package com.example.camscanner.util

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

object PerspectiveTransform {

    fun warpPerspective(bitmap: Bitmap, corners: List<PointF>): Bitmap {
        require(corners.size == 4) { "Must provide exactly 4 corners" }

        val src = Mat()
        Utils.bitmapToMat(bitmap, src)

        val orderedCorners = orderCorners(corners)
        val (width, height) = calculateOutputSize(orderedCorners)

        val srcPoints = MatOfPoint2f(
            Point(orderedCorners[0].x.toDouble(), orderedCorners[0].y.toDouble()),
            Point(orderedCorners[1].x.toDouble(), orderedCorners[1].y.toDouble()),
            Point(orderedCorners[2].x.toDouble(), orderedCorners[2].y.toDouble()),
            Point(orderedCorners[3].x.toDouble(), orderedCorners[3].y.toDouble())
        )

        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(width.toDouble(), 0.0),
            Point(width.toDouble(), height.toDouble()),
            Point(0.0, height.toDouble())
        )

        val transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
        val dst = Mat()
        Imgproc.warpPerspective(src, dst, transformMatrix, Size(width.toDouble(), height.toDouble()))

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, result)

        src.release(); dst.release(); transformMatrix.release()

        return result
    }

    private fun orderCorners(corners: List<PointF>): List<PointF> {
        val sorted = corners.sortedBy { it.y }
        val top = sorted.take(2).sortedBy { it.x }
        val bottom = sorted.drop(2).sortedBy { it.x }
        return listOf(top[0], top[1], bottom[1], bottom[0])
    }

    private fun calculateOutputSize(corners: List<PointF>): Pair<Int, Int> {
        val width = maxOf(
            distance(corners[0], corners[1]),
            distance(corners[3], corners[2])
        ).toInt()
        val height = maxOf(
            distance(corners[0], corners[3]),
            distance(corners[1], corners[2])
        ).toInt()
        return Pair(width, height)
    }

    private fun distance(p1: PointF, p2: PointF): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
}
