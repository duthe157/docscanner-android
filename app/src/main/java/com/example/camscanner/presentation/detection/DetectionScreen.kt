package com.example.camscanner.presentation.detection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.sqrt

@Composable
fun DetectionScreen(
    imageUri: String,
    viewModel: DetectionViewModel = hiltViewModel(),
    onConfirm: (Bitmap, List<PointF>) -> Unit = { _, _ -> },
    onRetake: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val corners by viewModel.corners.collectAsState()

    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var zoomedCornerIndex by remember { mutableStateOf(-1) }  // Req 4.3: -1 = no zoom

    // Keep latest corners accessible inside gesture handler without recreating it
    val cornersRef = rememberUpdatedState(corners)
    val imageSizeRef = rememberUpdatedState(imageSize)

    LaunchedEffect(imageUri) {
        val decoded = Uri.parse(imageUri).path?.let { BitmapFactory.decodeFile(it) }
        originalBitmap = decoded
        viewModel.detectEdges(imageUri)
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Chụp lại", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { viewModel.resetCorners() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset góc", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { viewModel.detectEdges(imageUri) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detect lại", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = {
                        val bmp = originalBitmap ?: return@Button
                        onConfirm(bmp, corners)
                    },
                    enabled = uiState is DetectionUiState.Success,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xác nhận", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is DetectionUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    Text(
                        "Đang phát hiện tài liệu...",
                        modifier = Modifier.align(Alignment.Center).padding(top = 60.dp)
                    )
                }

                is DetectionUiState.Success -> {
                    val bmp = originalBitmap ?: return@Box

                    // Image display
                    androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { imageSize = it }
                    )

                    if (corners.size == 4 && imageSize != IntSize.Zero) {
                        val scale = minOf(
                            imageSize.width.toFloat() / bmp.width,
                            imageSize.height.toFloat() / bmp.height
                        )
                        val offsetX = (imageSize.width - bmp.width * scale) / 2f
                        val offsetY = (imageSize.height - bmp.height * scale) / 2f

                        // Draw overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pts = corners.map { pt ->
                                Offset(pt.x * scale + offsetX, pt.y * scale + offsetY)
                            }
                            val path = Path().apply {
                                moveTo(pts[0].x, pts[0].y)
                                lineTo(pts[1].x, pts[1].y)
                                lineTo(pts[2].x, pts[2].y)
                                lineTo(pts[3].x, pts[3].y)
                                close()
                            }
                            drawPath(path, Color(0x3300AAFF))
                            drawPath(path, Color(0xFF00AAFF.toInt()), style = Stroke(width = 2.5.dp.toPx()))

                            pts.forEachIndexed { i, pt ->
                                val isActive = i == zoomedCornerIndex
                                drawCircle(Color(0x5500AAFF), radius = 28.dp.toPx(), center = pt)
                                drawCircle(if (isActive) Color(0xFFFFD700.toInt()) else Color.White, radius = 16.dp.toPx(), center = pt)
                                drawCircle(if (isActive) Color(0xFFFF8C00.toInt()) else Color(0xFF0088FF.toInt()), radius = 11.dp.toPx(), center = pt)
                            }
                        }

                        var activeCornerIndex by remember { mutableStateOf(-1) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val currentCorners = cornersRef.value
                                            val currentSize = imageSizeRef.value
                                            if (currentCorners.size != 4 || currentSize == IntSize.Zero) return@detectDragGestures

                                            val bmpW = originalBitmap?.width ?: return@detectDragGestures
                                            val bmpH = originalBitmap?.height ?: return@detectDragGestures
                                            val s = minOf(
                                                currentSize.width.toFloat() / bmpW,
                                                currentSize.height.toFloat() / bmpH
                                            )
                                            val ox = (currentSize.width - bmpW * s) / 2f
                                            val oy = (currentSize.height - bmpH * s) / 2f

                                            val touchRadiusPx = 60.dp.toPx()
                                            activeCornerIndex = currentCorners.indices.minByOrNull { i ->
                                                val cx = currentCorners[i].x * s + ox
                                                val cy = currentCorners[i].y * s + oy
                                                val dx = offset.x - cx
                                                val dy = offset.y - cy
                                                sqrt(dx * dx + dy * dy)
                                            }.let { idx ->
                                                if (idx != null) {
                                                    val cx = currentCorners[idx].x * s + ox
                                                    val cy = currentCorners[idx].y * s + oy
                                                    val dx = offset.x - cx
                                                    val dy = offset.y - cy
                                                    if (sqrt(dx * dx + dy * dy) <= touchRadiusPx) idx else -1
                                                } else -1
                                            }
                                            // Auto-zoom active corner (Req 4.3)
                                            if (activeCornerIndex >= 0) zoomedCornerIndex = activeCornerIndex
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val idx = activeCornerIndex
                                            if (idx < 0) return@detectDragGestures

                                            val currentCorners = cornersRef.value
                                            val currentSize = imageSizeRef.value
                                            val bmpW = originalBitmap?.width ?: return@detectDragGestures
                                            val bmpH = originalBitmap?.height ?: return@detectDragGestures

                                            val s = minOf(
                                                currentSize.width.toFloat() / bmpW,
                                                currentSize.height.toFloat() / bmpH
                                            )

                                            val current = currentCorners[idx]
                                            val newX = (current.x + dragAmount.x / s).coerceIn(0f, bmpW.toFloat())
                                            val newY = (current.y + dragAmount.y / s).coerceIn(0f, bmpH.toFloat())
                                            viewModel.updateCorner(idx, PointF(newX, newY))
                                        },
                                        onDragEnd = { activeCornerIndex = -1; zoomedCornerIndex = -1 },
                                        onDragCancel = { activeCornerIndex = -1; zoomedCornerIndex = -1 }
                                    )
                                }
                        )

                        // Zoom corner overlay (Req 4.3)
                        if (zoomedCornerIndex >= 0 && zoomedCornerIndex < corners.size) {
                            CornerZoomOverlay(
                                bitmap = bmp,
                                corner = corners[zoomedCornerIndex],
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }

                is DetectionUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Không thể phát hiện tài liệu", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.detectEdges(imageUri) }) { Text("Thử lại") }
                    }
                }
            }
        }
    }
}

/** Phóng to vùng xung quanh góc đang chỉnh — Req 4.3 */
@Composable
private fun CornerZoomOverlay(
    bitmap: Bitmap,
    corner: PointF,
    modifier: Modifier = Modifier
) {
    val zoomRadius = 80  // pixels in source image to crop around corner
    val srcX = (corner.x - zoomRadius).coerceIn(0f, (bitmap.width - zoomRadius * 2).toFloat()).toInt()
    val srcY = (corner.y - zoomRadius).coerceIn(0f, (bitmap.height - zoomRadius * 2).toFloat()).toInt()
    val srcW = (zoomRadius * 2).coerceAtMost(bitmap.width - srcX)
    val srcH = (zoomRadius * 2).coerceAtMost(bitmap.height - srcY)

    if (srcW <= 0 || srcH <= 0) return

    val cropped = remember(corner.x, corner.y) {
        android.graphics.Bitmap.createBitmap(bitmap, srcX, srcY, srcW, srcH)
    }

    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.7f))) {
        androidx.compose.foundation.Image(
            bitmap = cropped.asImageBitmap(),
            contentDescription = "Zoom góc",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Crosshair at center
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val len = 12.dp.toPx()
            val stroke = Stroke(width = 1.5.dp.toPx())
            drawLine(Color.Red, Offset(cx - len, cy), Offset(cx + len, cy), strokeWidth = stroke.width)
            drawLine(Color.Red, Offset(cx, cy - len), Offset(cx, cy + len), strokeWidth = stroke.width)
            drawCircle(Color.Red, radius = 4.dp.toPx(), center = Offset(cx, cy), style = stroke)
        }
    }
}
