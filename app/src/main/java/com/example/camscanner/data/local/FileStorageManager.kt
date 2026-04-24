package com.example.camscanner.data.local

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageManager @Inject constructor(
    private val context: Context
) {
    private val filesDir = context.filesDir

    private val originalsDir = File(filesDir, "originals").apply { mkdirs() }
    private val processedDir = File(filesDir, "processed").apply { mkdirs() }
    private val previewsDir = File(filesDir, "previews").apply { mkdirs() }
    private val exportsDir = File(filesDir, "exports").apply { mkdirs() }
    private val tempDir = File(context.cacheDir, "temp").apply { mkdirs() }

    fun saveOriginal(pageId: String, bitmap: Bitmap): String {
        checkStorageAvailable(bitmap.byteCount.toLong() * 2)
        val file = File(originalsDir, "${pageId}_orig.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file.absolutePath
    }

    fun saveProcessed(pageId: String, bitmap: Bitmap): String {
        val file = File(processedDir, "${pageId}_proc.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun savePreview(pageId: String, bitmap: Bitmap): String {
        val file = File(previewsDir, "${pageId}_thumb.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
        }
        return file.absolutePath
    }

    fun saveExport(docId: String, data: ByteArray, extension: String): String {
        val file = File(exportsDir, "$docId.$extension")
        file.writeBytes(data)
        return file.absolutePath
    }

    fun deletePageFiles(pageId: String) {
        File(originalsDir, "${pageId}_orig.jpg").delete()
        File(processedDir, "${pageId}_proc.jpg").delete()
        File(previewsDir, "${pageId}_thumb.jpg").delete()
    }

    fun getTempDir(): File = tempDir

    fun calculateTotalSize(): Long {
        return listOf(originalsDir, processedDir, previewsDir, exportsDir)
            .sumOf { dir -> dir.walkTopDown().filter { it.isFile }.sumOf { it.length() } }
    }

    fun deleteOriginals() {
        originalsDir.listFiles()?.forEach { it.delete() }
    }

    /** Throws [InsufficientStorageException] if free space < required bytes */
    private fun checkStorageAvailable(requiredBytes: Long) {
        val freeBytes = filesDir.freeSpace
        if (freeBytes < requiredBytes + MIN_FREE_BYTES) {
            throw InsufficientStorageException(
                "Bộ nhớ không đủ. Cần thêm ${formatBytes(requiredBytes)} để lưu file."
            )
        }
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
    }

    companion object {
        private const val MIN_FREE_BYTES = 10 * 1024 * 1024L  // 10 MB buffer
    }
}

class InsufficientStorageException(message: String) : Exception(message)
