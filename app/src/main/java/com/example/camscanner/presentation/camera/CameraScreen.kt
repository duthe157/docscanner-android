package com.example.camscanner.presentation.camera

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onImageCaptured: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val uiState by viewModel.uiState.collectAsState()
    val flashEnabled by viewModel.flashEnabled.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is CameraUiState.Captured) {
            val uri = (uiState as CameraUiState.Captured).imageUri
            onImageCaptured(uri.toString())
            viewModel.resetState()
        }
    }

    // ── Permission denied UI ──────────────────────────────────────────────────
    if (!cameraPermissionState.status.isGranted) {
        PermissionDeniedScreen(
            shouldShowRationale = cameraPermissionState.status.shouldShowRationale,
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            }
        )
        return
    }

    // ── Camera UI ─────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Camera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                        .build()
                    imageCapture = capture
                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                        // Continuous auto-focus
                        val meteringPoint = previewView.meteringPointFactory
                            .createPoint(previewView.width / 2f, previewView.height / 2f)
                        val action = FocusMeteringAction.Builder(meteringPoint)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        camera?.cameraControl?.startFocusAndMetering(action)
                    } catch (e: Exception) {
                        viewModel.onCaptureError(e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Tap-to-focus
                        val meteringPointFactory = androidx.camera.view.PreviewView(context).meteringPointFactory
                        camera?.cameraControl?.startFocusAndMetering(
                            FocusMeteringAction.Builder(
                                meteringPointFactory.createPoint(offset.x, offset.y)
                            ).build()
                        )
                    }
                }
        )

        // Guide frame overlay
        GuideFrameOverlay(modifier = Modifier.fillMaxSize())

        // Top bar — flash toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { viewModel.toggleFlash() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashEnabled) "Tắt flash" else "Bật flash",
                    tint = if (flashEnabled) Color.Yellow else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Đặt tài liệu vào khung hướng dẫn",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            // Shutter button
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                )
                // Inner button
                FilledIconButton(
                    onClick = {
                        if (!isCapturing) {
                            isCapturing = true
                            val outputFile = viewModel.getTempImageFile()
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
                            imageCapture?.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        viewModel.onPhotoCaptured(android.net.Uri.fromFile(outputFile))
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        viewModel.onCaptureError(exception)
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isCapturing) Color.Gray else Color.White
                    )
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.DarkGray,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Chụp ảnh",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Error snackbar
        if (uiState is CameraUiState.Error) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                }
            ) {
                Text((uiState as CameraUiState.Error).message)
            }
        }
    }
}

/** Semi-transparent overlay with a rounded-rect cutout as document guide frame */
@Composable
private fun GuideFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val frameW = size.width * 0.85f
        val frameH = frameW * 1.414f  // A4 ratio
        val left = (size.width - frameW) / 2f
        val top = (size.height - frameH) / 2f - size.height * 0.04f

        // Dark scrim
        drawRect(Color.Black.copy(alpha = 0.45f))

        // Clear the guide area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(12.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Blue border
        drawRoundRect(
            color = Color(0xFF2196F3),
            topLeft = Offset(left, top),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Corner accents
        val accentLen = 24.dp.toPx()
        val stroke = Stroke(width = 3.dp.toPx())
        val corners = listOf(
            Offset(left, top) to Pair(Offset(left + accentLen, top), Offset(left, top + accentLen)),
            Offset(left + frameW, top) to Pair(Offset(left + frameW - accentLen, top), Offset(left + frameW, top + accentLen)),
            Offset(left, top + frameH) to Pair(Offset(left + accentLen, top + frameH), Offset(left, top + frameH - accentLen)),
            Offset(left + frameW, top + frameH) to Pair(Offset(left + frameW - accentLen, top + frameH), Offset(left + frameW, top + frameH - accentLen))
        )
        corners.forEach { (corner, lines) ->
            drawLine(Color.White, corner, lines.first, strokeWidth = stroke.width)
            drawLine(Color.White, corner, lines.second, strokeWidth = stroke.width)
        }
    }
}

@Composable
private fun PermissionDeniedScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Cần quyền truy cập camera",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (shouldShowRationale)
                    "App cần quyền camera để chụp ảnh tài liệu. Vui lòng cấp quyền để tiếp tục."
                else
                    "Quyền camera đã bị từ chối. Vào Cài đặt để cấp quyền thủ công.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(24.dp))
            if (shouldShowRationale) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cấp quyền camera")
                }
            } else {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mở Cài đặt")
                }
            }
        }
    }
}
