package com.example.camscanner.domain.usecase

import android.graphics.Bitmap
import android.graphics.PointF
import com.example.camscanner.domain.model.FilterType
import com.example.camscanner.util.ImageUtils
import com.example.camscanner.util.PerspectiveTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import javax.inject.Inject

class ProcessImageUseCase @Inject constructor() {

    suspend fun execute(
        bitmap: Bitmap,
        corners: List<PointF> = emptyList(),
        filter: FilterType = FilterType.ORIGINAL,
        brightness: Int = 0,
        contrast: Int = 0,
        rotation: Int = 0,
        isPreview: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        var result = bitmap

        // 1. Perspective warp on original resolution
        if (corners.size == 4) {
            result = try { PerspectiveTransform.warpPerspective(result, corners) }
            catch (_: Exception) { result }
        }

        // 2. Resize after warp
        result = ImageUtils.resizeForDetection(result, if (isPreview) 800 else 2048)

        // 3. Scan processing
        result = try { scanProcess(result, filter) }
        catch (_: Exception) { ImageUtils.applyFilter(result, filter) }

        // 4. Brightness/contrast
        if (brightness != 0 || contrast != 0) {
            result = ImageUtils.adjustBrightnessContrast(result, brightness, contrast)
        }

        // 5. Sharpen on export only
        if (!isPreview) result = ImageUtils.sharpen(result)

        // 6. Rotation
        repeat(rotation / 90) { result = ImageUtils.rotate90(result) }

        result
    }

    private fun scanProcess(bitmap: Bitmap, filter: FilterType): Bitmap {
        val rgba = Mat()
        Utils.bitmapToMat(bitmap, rgba)

        val processed = when (filter) {
            FilterType.BW      -> bwScan(rgba)
            FilterType.COLOR   -> colorScan(rgba)
            FilterType.DARK    -> darkScan(rgba)
            FilterType.ORIGINAL -> lightNormalize(rgba)
        }

        val out = Bitmap.createBitmap(processed.cols(), processed.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(processed, out)
        rgba.release(); processed.release()
        return out
    }

    /**
     * B&W scan — same as working version (block=11, C=10) + light Gaussian to reduce noise dots.
     * Pipeline: RGBA → Gray → GaussianBlur(3) → CLAHE → adaptiveThreshold(11,10) → unsharp → RGBA
     */
    private fun bwScan(rgba: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)

        // Light Gaussian blur to reduce noise before threshold (kernel=3, not 9 — keeps text sharp)
        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(3.0, 3.0), 0.0)

        // CLAHE: normalize uneven lighting
        val equalized = Mat()
        Imgproc.createCLAHE(3.0, Size(8.0, 8.0)).apply(blurred, equalized)

        // Adaptive threshold — same params as working version
        val thresh = Mat()
        Imgproc.adaptiveThreshold(
            equalized, thresh, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            11, 10.0
        )

        // Auto-invert if background is dark
        if (Core.mean(thresh).`val`[0] < 127.0) {
            Core.bitwise_not(thresh, thresh)
        }

        // Unsharp mask to make text crisper (subtle — just sharpens edges)
        val blurForSharp = Mat()
        Imgproc.GaussianBlur(thresh, blurForSharp, Size(0.0, 0.0), 1.0)
        val sharpened = Mat()
        Core.addWeighted(thresh, 1.5, blurForSharp, -0.5, 0.0, sharpened)

        val result = Mat()
        Imgproc.cvtColor(sharpened, result, Imgproc.COLOR_GRAY2RGBA)

        gray.release(); blurred.release(); equalized.release()
        thresh.release(); blurForSharp.release(); sharpened.release()
        return result
    }

    /**
     * Color scan — CLAHE on LAB L-channel.
     */
    private fun colorScan(rgba: Mat): Mat {
        val rgb = Mat()
        Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB)

        val lab = Mat()
        Imgproc.cvtColor(rgb, lab, Imgproc.COLOR_RGB2Lab)

        val channels = ArrayList<Mat>()
        Core.split(lab, channels)
        Imgproc.createCLAHE(2.5, Size(8.0, 8.0)).apply(channels[0], channels[0])
        Core.merge(channels, lab)

        val resultRgb = Mat()
        Imgproc.cvtColor(lab, resultRgb, Imgproc.COLOR_Lab2RGB)

        val result = Mat()
        Imgproc.cvtColor(resultRgb, result, Imgproc.COLOR_RGB2RGBA)

        rgb.release(); lab.release(); resultRgb.release()
        channels.forEach { it.release() }
        return result
    }

    /**
     * Dark document — inverted adaptive threshold.
     */
    private fun darkScan(rgba: Mat): Mat {
        val gray = Mat()
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)

        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(3.0, 3.0), 0.0)

        val equalized = Mat()
        Imgproc.createCLAHE(2.0, Size(8.0, 8.0)).apply(blurred, equalized)

        val thresh = Mat()
        Imgproc.adaptiveThreshold(
            equalized, thresh, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            11, 8.0
        )

        val result = Mat()
        Imgproc.cvtColor(thresh, result, Imgproc.COLOR_GRAY2RGBA)

        gray.release(); blurred.release(); equalized.release(); thresh.release()
        return result
    }

    /**
     * Original — mild CLAHE only, keep colors natural.
     */
    private fun lightNormalize(rgba: Mat): Mat {
        val rgb = Mat()
        Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB)

        val lab = Mat()
        Imgproc.cvtColor(rgb, lab, Imgproc.COLOR_RGB2Lab)

        val channels = ArrayList<Mat>()
        Core.split(lab, channels)
        Imgproc.createCLAHE(1.5, Size(8.0, 8.0)).apply(channels[0], channels[0])
        Core.merge(channels, lab)

        val resultRgb = Mat()
        Imgproc.cvtColor(lab, resultRgb, Imgproc.COLOR_Lab2RGB)

        val result = Mat()
        Imgproc.cvtColor(resultRgb, result, Imgproc.COLOR_RGB2RGBA)

        rgb.release(); lab.release(); resultRgb.release()
        channels.forEach { it.release() }
        return result
    }
}
