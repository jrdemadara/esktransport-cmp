package org.noztek.esktransport.feature.driver.onboarding.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
actual fun DriverDocumentCaptureScreen(
    type: DriverOnboardingDocumentType,
    onCaptured: (CapturedDocumentImage) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { MainThreadExecutor() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    var hasCameraPermission by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var assessment by remember { mutableStateOf(CaptureAssessment(isGood = false, message = type.initialPrompt(), progress = 0f)) }
    var stableSinceMs by remember { mutableStateOf<Long?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission, type) {
        if (!hasCameraPermission) return@LaunchedEffect

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                val provider = providerFuture.get()
                cameraProvider = provider

                val preview = CameraPreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setJpegQuality(88)
                    .build()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analyzer ->
                        analyzer.setAnalyzer(cameraExecutor) { proxy ->
                            val frameAssessment = proxy.assessForCapture(type)
                            proxy.close()
                            previewView.post {
                                val now = System.currentTimeMillis()
                                val firstStable = if (frameAssessment.isGood) stableSinceMs ?: now else null
                                stableSinceMs = firstStable
                                val progress = firstStable?.let { ((now - it).toFloat() / 1000f).coerceIn(0f, 1f) } ?: 0f
                                assessment = frameAssessment.copy(progress = progress)
                                if (progress >= 1f && !isCapturing) {
                                    isCapturing = true
                                    captureDocument(
                                        type = type,
                                        contextCacheDir = context.cacheDir,
                                        imageCapture = capture,
                                        executor = cameraExecutor,
                                        mainExecutor = mainExecutor,
                                        onCaptured = onCaptured,
                                        onError = {
                                            isCapturing = false
                                            stableSinceMs = null
                                            assessment = CaptureAssessment(
                                                isGood = false,
                                                message = "Capture failed. Hold steady and try again.",
                                                progress = 0f,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                val lensFacing = if (type == DriverOnboardingDocumentType.Selfie) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, capture, analysis)
                imageCapture = capture
            },
            mainExecutor,
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionContent(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onClose = onClose,
        )
        return
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { previewView },
            )
            CaptureGuideOverlay(type = type, isGood = assessment.isGood)
            CaptureTopBar(title = type.captureTitle(), onClose = onClose)
            CaptureBottomPanel(
                assessment = assessment,
                isCapturing = isCapturing,
                onManualCapture = {
                    val capture = imageCapture ?: return@CaptureBottomPanel
                    isCapturing = true
                    captureDocument(
                        type = type,
                        contextCacheDir = context.cacheDir,
                        imageCapture = capture,
                        executor = cameraExecutor,
                        mainExecutor = mainExecutor,
                        onCaptured = onCaptured,
                        onError = {
                            isCapturing = false
                            stableSinceMs = null
                            assessment = CaptureAssessment(
                                isGood = false,
                                message = "Capture failed. Try again.",
                                progress = 0f,
                            )
                        },
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Camera permission is required.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Use the camera to capture your driver setup documents inside the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppPrimaryButton(text = "Allow camera", onClick = onRequestPermission)
            OutlinedButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun CaptureTopBar(
    title: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Heroicons.Outline.ArrowLeft,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun CaptureGuideOverlay(
    type: DriverOnboardingDocumentType,
    isGood: Boolean,
) {
    val guideColor = if (isGood) Color(0xFF22C55E) else Color.White
    Canvas(modifier = Modifier.fillMaxSize()) {
        val guideWidth = size.width * 0.84f
        val guideHeight = if (type == DriverOnboardingDocumentType.Selfie) {
            guideWidth
        } else {
            guideWidth * 0.62f
        }
        val left = (size.width - guideWidth) / 2f
        val top = (size.height - guideHeight) / 2f - size.height * 0.04f
        val cornerRadius = if (type == DriverOnboardingDocumentType.Selfie) {
            guideWidth / 2f
        } else {
            28f
        }
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset.Zero,
            size = size,
        )
        drawRoundRect(
            color = guideColor,
            topLeft = Offset(left, top),
            size = Size(guideWidth, guideHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 5f),
        )
    }
}

@Composable
private fun CaptureBottomPanel(
    assessment: CaptureAssessment,
    isCapturing: Boolean,
    onManualCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = assessment.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (assessment.isGood) {
                LinearProgressIndicator(
                    progress = { assessment.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppPrimaryButton(
                    text = if (isCapturing) "Capturing..." else "Capture now",
                    onClick = onManualCapture,
                    enabled = !isCapturing,
                    modifier = Modifier.weight(1f),
                )
                if (isCapturing) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun captureDocument(
    type: DriverOnboardingDocumentType,
    contextCacheDir: File,
    imageCapture: ImageCapture,
    executor: Executor,
    mainExecutor: Executor,
    onCaptured: (CapturedDocumentImage) -> Unit,
    onError: () -> Unit,
) {
    val file = File(contextCacheDir, "driver_${type.apiValue}_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bytes = file.readBytes()
                file.delete()
                mainExecutor.execute {
                    onCaptured(
                        CapturedDocumentImage(
                            fileName = file.name,
                            mimeType = "image/jpeg",
                            bytes = bytes,
                        ),
                    )
                }
            }

            override fun onError(exception: ImageCaptureException) {
                file.delete()
                mainExecutor.execute(onError)
            }
        },
    )
}

private data class CaptureAssessment(
    val isGood: Boolean,
    val message: String,
    val progress: Float,
)

private fun ImageProxy.assessForCapture(type: DriverOnboardingDocumentType): CaptureAssessment {
    val plane = planes.firstOrNull() ?: return CaptureAssessment(false, "Camera frame is not ready.", 0f)
    val buffer = plane.buffer
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val step = 18
    var brightnessTotal = 0L
    var brightnessCount = 0
    var edgeTotal = 0L
    var edgeCount = 0

    fun yAt(x: Int, y: Int): Int {
        val index = y * rowStride + x * pixelStride
        return if (index in 0 until buffer.limit()) buffer.get(index).toInt() and 0xFF else 0
    }

    var y = step
    while (y < height - step) {
        var x = step
        while (x < width - step) {
            val center = yAt(x, y)
            brightnessTotal += center
            brightnessCount += 1
            edgeTotal += kotlin.math.abs(center - yAt(x + step, y))
            edgeTotal += kotlin.math.abs(center - yAt(x, y + step))
            edgeCount += 2
            x += step
        }
        y += step
    }

    val brightness = if (brightnessCount == 0) 0 else brightnessTotal / brightnessCount
    val sharpness = if (edgeCount == 0) 0 else edgeTotal / edgeCount

    return when {
        brightness < 55 -> CaptureAssessment(false, "Move to a brighter area.", 0f)
        brightness > 220 -> CaptureAssessment(false, "Reduce glare on the ${type.shortLabel()}.", 0f)
        sharpness < 7 -> CaptureAssessment(false, "Hold steady and keep the ${type.shortLabel()} sharp.", 0f)
        else -> CaptureAssessment(true, "Good. Hold steady for auto capture.", 0f)
    }
}

private fun DriverOnboardingDocumentType.captureTitle(): String {
    return when (this) {
        DriverOnboardingDocumentType.LicenseFront -> "Capture license front"
        DriverOnboardingDocumentType.LicenseBack -> "Capture license back"
        DriverOnboardingDocumentType.Selfie -> "Capture selfie"
        DriverOnboardingDocumentType.VehicleRegistration -> "Capture vehicle registration"
    }
}

private fun DriverOnboardingDocumentType.initialPrompt(): String {
    return if (this == DriverOnboardingDocumentType.Selfie) {
        "Place your face inside the guide."
    } else {
        "Place the ${shortLabel()} inside the guide."
    }
}

private fun DriverOnboardingDocumentType.shortLabel(): String {
    return when (this) {
        DriverOnboardingDocumentType.LicenseFront,
        DriverOnboardingDocumentType.LicenseBack -> "card"
        DriverOnboardingDocumentType.Selfie -> "selfie"
        DriverOnboardingDocumentType.VehicleRegistration -> "document"
    }
}

private class MainThreadExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}
