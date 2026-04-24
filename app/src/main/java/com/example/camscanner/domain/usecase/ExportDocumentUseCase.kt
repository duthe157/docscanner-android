package com.example.camscanner.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import com.example.camscanner.data.local.FileStorageManager
import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.ExportFormat
import com.example.camscanner.domain.model.ExportOptions
import com.example.camscanner.domain.model.Page
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ExportDocumentUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileStorageManager: FileStorageManager
) {
    suspend fun exportToPdf(document: Document, options: ExportOptions): String =
        withContext(Dispatchers.IO) {
            val pages = if (options.pageRange != null) {
                document.pages.filter { it.order in options.pageRange }
            } else {
                document.pages
            }.sortedBy { it.order }

            val pdfDoc = PdfDocument()
            pages.forEachIndexed { index, page ->
                val bitmap = loadBitmap(page.processedPath) ?: return@forEachIndexed
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val pdfPage = pdfDoc.startPage(pageInfo)
                pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDoc.finishPage(pdfPage)
                bitmap.recycle()
            }

            val out = ByteArrayOutputStream()
            pdfDoc.writeTo(out)
            pdfDoc.close()

            fileStorageManager.saveExport(document.id, out.toByteArray(), "pdf")
        }

    suspend fun exportToImage(page: Page, format: ExportFormat, quality: Int = 90): String =
        withContext(Dispatchers.IO) {
            val bitmap = loadBitmap(page.processedPath)
                ?: error("Không thể đọc ảnh trang ${page.id}")

            val out = ByteArrayOutputStream()
            when (format) {
                ExportFormat.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                ExportFormat.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                ExportFormat.PDF -> {
                    val pdfDoc = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                    val pdfPage = pdfDoc.startPage(pageInfo)
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDoc.finishPage(pdfPage)
                    pdfDoc.writeTo(out)
                    pdfDoc.close()
                }
            }
            bitmap.recycle()

            val ext = when (format) {
                ExportFormat.JPG -> "jpg"
                ExportFormat.PNG -> "png"
                ExportFormat.PDF -> "pdf"
            }
            fileStorageManager.saveExport("${page.documentId}_${page.id}", out.toByteArray(), ext)
        }

    private fun loadBitmap(path: String): Bitmap? {
        return try { BitmapFactory.decodeFile(path) } catch (_: Exception) { null }
    }
}
