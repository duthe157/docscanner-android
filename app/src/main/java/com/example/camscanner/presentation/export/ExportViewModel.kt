package com.example.camscanner.presentation.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camscanner.domain.model.Document
import com.example.camscanner.domain.model.ExportFormat
import com.example.camscanner.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ExportUiState(
    val document: Document? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,   // 0..1 for multi-page PDF
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun loadDocument(documentId: String) {
        // Apply default export format from Settings (Req 11.2)
        val defaultFormat = context.getSharedPreferences("camscanner_settings", Context.MODE_PRIVATE)
            .getString("default_export_format", ExportFormat.PDF.name)
            ?.let { runCatching { ExportFormat.valueOf(it) }.getOrDefault(ExportFormat.PDF) }
            ?: ExportFormat.PDF
        _uiState.value = _uiState.value.copy(selectedFormat = defaultFormat)

        if (documentId == "temp") {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        viewModelScope.launch {
            try {
                val docs = documentRepository.getDocuments().first()
                val doc = docs.find { it.id == documentId }
                _uiState.value = _uiState.value.copy(document = doc, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format, message = null, error = null)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun exportAndSave(fallbackBitmap: Bitmap? = null) {
        _uiState.value = _uiState.value.copy(isExporting = true, error = null, message = null, exportProgress = 0f)
        viewModelScope.launch {
            try {
                val format = _uiState.value.selectedFormat
                withContext(Dispatchers.IO) {
                    val doc = _uiState.value.document
                    if (doc != null) exportDocumentToStorage(doc, format)
                    else if (fallbackBitmap != null) exportBitmapToStorage(fallbackBitmap, format)
                    else error("Không có dữ liệu để xuất")
                }
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 1f, message = "Đã lưu vào Documents/CamScanner")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 0f, error = "Lỗi: ${e.message}")
            }
        }
    }

    fun share(fallbackBitmap: Bitmap? = null) {
        _uiState.value = _uiState.value.copy(isExporting = true, error = null, exportProgress = 0f)
        viewModelScope.launch {
            try {
                val format = _uiState.value.selectedFormat
                val cacheUri = withContext(Dispatchers.IO) {
                    val doc = _uiState.value.document
                    if (doc != null) exportDocumentToCache(doc, format)
                    else if (fallbackBitmap != null) exportBitmapToCache(fallbackBitmap, format)
                    else error("Không có dữ liệu để chia sẻ")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = format.mimeType
                    putExtra(Intent.EXTRA_STREAM, cacheUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Chia sẻ tài liệu").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 1f)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 0f, error = "Lỗi: ${e.message}")
            }
        }
    }

    fun saveToPhotos(fallbackBitmap: Bitmap? = null) {
        _uiState.value = _uiState.value.copy(isExporting = true, error = null, message = null, exportProgress = 0f)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val doc = _uiState.value.document
                    val pages = doc?.pages?.sortedBy { it.order }
                    if (!pages.isNullOrEmpty()) {
                        // Save each page to Photos
                        pages.forEachIndexed { i, page ->
                            _uiState.value = _uiState.value.copy(exportProgress = (i + 1f) / pages.size)
                            val bmp = BitmapFactory.decodeFile(page.processedPath) ?: return@forEachIndexed
                            saveImageToPhotos(bmp, "scan_${System.currentTimeMillis()}_$i.jpg")
                            bmp.recycle()
                        }
                    } else if (fallbackBitmap != null) {
                        saveImageToPhotos(fallbackBitmap, "scan_${System.currentTimeMillis()}.jpg")
                    } else {
                        error("Không có ảnh để lưu")
                    }
                }
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 1f, message = "Đã lưu vào Thư viện ảnh")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, exportProgress = 0f, error = "Lỗi: ${e.message}")
            }
        }
    }

    private fun saveImageToPhotos(bitmap: Bitmap, fileName: String) {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        val bytes = out.toByteArray()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CamScanner")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Không thể lưu ảnh")
            context.contentResolver.openOutputStream(uri)!!.use { it.write(bytes) }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CamScanner")
            dir.mkdirs()
            File(dir, fileName).writeBytes(bytes)
        }
    }

    private fun exportDocumentToStorage(doc: Document, format: ExportFormat) {
        val pages = doc.pages.sortedBy { it.order }
        val name = "${doc.name.sanitize()}_${System.currentTimeMillis()}.${format.ext}"
        val bytes = buildExportBytes(pages.map { it.processedPath }, format)
        saveToMediaStore(bytes, name, format.mimeType)
    }

    private fun exportDocumentToCache(doc: Document, format: ExportFormat): Uri {
        val pages = doc.pages.sortedBy { it.order }
        val name = "${doc.name.sanitize()}_${System.currentTimeMillis()}.${format.ext}"
        val bytes = buildExportBytes(pages.map { it.processedPath }, format)
        return saveToCacheFile(bytes, name)
    }

    private fun exportBitmapToStorage(bitmap: Bitmap, format: ExportFormat) {
        val name = "scan_${System.currentTimeMillis()}.${format.ext}"
        val bytes = bitmapToBytes(bitmap, format)
        saveToMediaStore(bytes, name, format.mimeType)
    }

    private fun exportBitmapToCache(bitmap: Bitmap, format: ExportFormat): Uri {
        val name = "scan_${System.currentTimeMillis()}.${format.ext}"
        val bytes = bitmapToBytes(bitmap, format)
        return saveToCacheFile(bytes, name)
    }

    // --- Byte builders ---

    private fun buildExportBytes(paths: List<String>, format: ExportFormat): ByteArray {
        return when (format) {
            ExportFormat.PDF -> buildPdfBytes(paths)
            ExportFormat.JPG -> {
                val bmp = BitmapFactory.decodeFile(paths.first()) ?: error("Không đọc được ảnh")
                bitmapToBytes(bmp, format).also { bmp.recycle() }
            }
            ExportFormat.PNG -> {
                val bmp = BitmapFactory.decodeFile(paths.first()) ?: error("Không đọc được ảnh")
                bitmapToBytes(bmp, format).also { bmp.recycle() }
            }
        }
    }

    private fun buildPdfBytes(paths: List<String>): ByteArray {
        val pdfDoc = PdfDocument()
        val total = paths.size
        paths.forEachIndexed { i, path ->
            _uiState.value = _uiState.value.copy(exportProgress = (i + 1).toFloat() / total)
            val bmp = BitmapFactory.decodeFile(path) ?: return@forEachIndexed
            val info = PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val page = pdfDoc.startPage(info)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            pdfDoc.finishPage(page)
            bmp.recycle()
        }
        val out = ByteArrayOutputStream()
        pdfDoc.writeTo(out)
        pdfDoc.close()
        return out.toByteArray()
    }

    private fun bitmapToBytes(bitmap: Bitmap, format: ExportFormat): ByteArray {
        val out = ByteArrayOutputStream()
        when (format) {
            ExportFormat.PDF -> {
                val pdfDoc = PdfDocument()
                val info = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDoc.startPage(info)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDoc.finishPage(page)
                pdfDoc.writeTo(out)
                pdfDoc.close()
            }
            ExportFormat.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            ExportFormat.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return out.toByteArray()
    }

    // --- Storage helpers ---

    private fun saveToMediaStore(bytes: ByteArray, fileName: String, mimeType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/CamScanner")
            }
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
                ?: error("Không thể tạo file")
            context.contentResolver.openOutputStream(uri)!!.use { it.write(bytes) }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "CamScanner")
            dir.mkdirs()
            File(dir, fileName).writeBytes(bytes)
        }
    }

    private fun saveToCacheFile(bytes: ByteArray, fileName: String): Uri {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun String.sanitize() = replace(Regex("[^a-zA-Z0-9_\\-àáâãèéêìíòóôõùúýăđơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ ]"), "_").take(40)
}

val ExportFormat.ext: String get() = when (this) { ExportFormat.PDF -> "pdf"; ExportFormat.JPG -> "jpg"; ExportFormat.PNG -> "png" }
val ExportFormat.mimeType: String get() = when (this) { ExportFormat.PDF -> "application/pdf"; ExportFormat.JPG -> "image/jpeg"; ExportFormat.PNG -> "image/png" }
val ExportFormat.label: String get() = when (this) { ExportFormat.PDF -> "PDF"; ExportFormat.JPG -> "JPEG"; ExportFormat.PNG -> "PNG" }
