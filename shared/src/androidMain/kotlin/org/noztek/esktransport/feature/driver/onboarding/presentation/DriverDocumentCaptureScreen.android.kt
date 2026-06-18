package org.noztek.esktransport.feature.driver.onboarding.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import java.io.ByteArrayOutputStream
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
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .setMinFaceSize(0.05f)
                .build(),
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
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
                            proxy.assessForCapture(
                                type = type,
                                faceDetector = faceDetector,
                                onAssessment = { frameAssessment ->
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
                                                context = context,
                                                imageCapture = capture,
                                                faceDetector = faceDetector,
                                                executor = cameraExecutor,
                                                mainExecutor = mainExecutor,
                                                onCaptured = onCaptured,
                                                onError = { message ->
                                                    isCapturing = false
                                                    stableSinceMs = null
                                                    assessment = CaptureAssessment(
                                                        isGood = false,
                                                        message = message,
                                                        progress = 0f,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                },
                            )
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
            },
            mainExecutor,
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            faceDetector.close()
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
        val guideBounds = guideBoundsFor(
            type = type,
            frameWidth = size.width,
            frameHeight = size.height,
        )
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset.Zero,
            size = size,
        )
        drawRoundRect(
            color = guideColor,
            topLeft = guideBounds.topLeft,
            size = guideBounds.size,
            cornerRadius = CornerRadius(guideBounds.cornerRadius, guideBounds.cornerRadius),
            style = Stroke(width = 5f),
        )
    }
}

@Composable
private fun CaptureBottomPanel(
    assessment: CaptureAssessment,
    isCapturing: Boolean,
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
            if (isCapturing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Capturing...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun captureDocument(
    type: DriverOnboardingDocumentType,
    context: Context,
    imageCapture: ImageCapture,
    faceDetector: com.google.mlkit.vision.face.FaceDetector,
    executor: Executor,
    mainExecutor: Executor,
    onCaptured: (CapturedDocumentImage) -> Unit,
    onError: (String) -> Unit,
) {
    val file = File(context.cacheDir, "driver_${type.apiValue}_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                if (type == DriverOnboardingDocumentType.Selfie) {
                    validateCapturedSelfie(
                        context = context,
                        file = file,
                        faceDetector = faceDetector,
                        mainExecutor = mainExecutor,
                        onValid = { bytes ->
                            onCaptured(
                                CapturedDocumentImage(
                                    fileName = file.name,
                                    mimeType = "image/jpeg",
                                    bytes = bytes,
                                ),
                            )
                        },
                        onInvalid = {
                            onError("No face detected. Keep your face inside the oval.")
                        },
                    )
                    return
                }

                val bytes = file.readCompressedJpegBytes()
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
                mainExecutor.execute {
                    onError("Capture failed. Hold steady and try again.")
                }
            }
        },
    )
}

private fun validateCapturedSelfie(
    context: Context,
    file: File,
    faceDetector: com.google.mlkit.vision.face.FaceDetector,
    mainExecutor: Executor,
    onValid: (ByteArray) -> Unit,
    onInvalid: () -> Unit,
) {
    val inputImage = try {
        InputImage.fromFilePath(context, Uri.fromFile(file))
    } catch (_: Throwable) {
        file.delete()
        mainExecutor.execute(onInvalid)
        return
    }

    faceDetector.process(inputImage)
        .addOnSuccessListener { faces ->
            val isValid = assessSelfieFace(
                faces = faces,
                frameWidth = inputImage.width,
                frameHeight = inputImage.height,
                applyPreviewAlignmentCorrection = false,
            ).isGood
            val bytes = if (isValid) file.readCompressedJpegBytes() else null
            file.delete()
            mainExecutor.execute {
                if (bytes != null) {
                    onValid(bytes)
                } else {
                    onInvalid()
                }
            }
        }
        .addOnFailureListener {
            file.delete()
            mainExecutor.execute(onInvalid)
        }
}

private fun File.readCompressedJpegBytes(): ByteArray {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(absolutePath, bounds)

    val maxDimension = 1600
    var sampleSize = 1
    while ((bounds.outWidth / sampleSize) > maxDimension || (bounds.outHeight / sampleSize) > maxDimension) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }
    val bitmap = BitmapFactory.decodeFile(absolutePath, options) ?: return readBytes()
    return ByteArrayOutputStream().use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 78, output)
        bitmap.recycle()
        output.toByteArray()
    }
}

private data class CaptureAssessment(
    val isGood: Boolean,
    val message: String,
    val progress: Float,
)

private data class CaptureGuideBounds(
    val topLeft: Offset,
    val size: Size,
    val cornerRadius: Float,
) {
    val centerX: Float = topLeft.x + size.width / 2f
    val centerY: Float = topLeft.y + size.height / 2f
}

private fun guideBoundsFor(
    type: DriverOnboardingDocumentType,
    frameWidth: Float,
    frameHeight: Float,
    selfieVerticalShiftFraction: Float = 0f,
): CaptureGuideBounds {
    val guideWidth = if (type == DriverOnboardingDocumentType.Selfie) {
        frameWidth * 0.72f
    } else {
        frameWidth * 0.84f
    }
    val guideHeight = if (type == DriverOnboardingDocumentType.Selfie) {
        (guideWidth * 1.28f).coerceAtMost(frameHeight * 0.58f)
    } else {
        guideWidth * 0.62f
    }
    val left = (frameWidth - guideWidth) / 2f
    val top = (frameHeight - guideHeight) / 2f - frameHeight * 0.04f + frameHeight * selfieVerticalShiftFraction
    val cornerRadius = if (type == DriverOnboardingDocumentType.Selfie) {
        guideWidth / 2f
    } else {
        28f
    }
    return CaptureGuideBounds(
        topLeft = Offset(left, top),
        size = Size(guideWidth, guideHeight),
        cornerRadius = cornerRadius,
    )
}

@OptIn(ExperimentalGetImage::class)
private fun ImageProxy.assessForCapture(
    type: DriverOnboardingDocumentType,
    faceDetector: com.google.mlkit.vision.face.FaceDetector,
    onAssessment: (CaptureAssessment) -> Unit,
) {
    if (type == DriverOnboardingDocumentType.Selfie) {
        assessSelfieForCapture(
            faceDetector = faceDetector,
            onAssessment = onAssessment,
        )
        return
    }

    val qualityAssessment = assessFrameQuality(type)
    if (!qualityAssessment.isGood) {
        close()
        onAssessment(qualityAssessment)
        return
    }

    close()
    onAssessment(qualityAssessment)
}

@OptIn(ExperimentalGetImage::class)
private fun ImageProxy.assessSelfieForCapture(
    faceDetector: com.google.mlkit.vision.face.FaceDetector,
    onAssessment: (CaptureAssessment) -> Unit,
) {
    val mediaImage = image
    if (mediaImage == null) {
        close()
        onAssessment(CaptureAssessment(false, "Camera frame is not ready.", 0f))
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageInfo.rotationDegrees)
    faceDetector.process(inputImage)
        .addOnSuccessListener { faces ->
            onAssessment(
                assessSelfieFace(
                    faces = faces,
                    frameWidth = inputImage.width,
                    frameHeight = inputImage.height,
                    applyPreviewAlignmentCorrection = true,
                ),
            )
        }
        .addOnFailureListener {
            onAssessment(CaptureAssessment(false, "Face detection is not ready. Try again.", 0f))
        }
        .addOnCompleteListener {
            close()
        }
}

private fun ImageProxy.assessFrameQuality(type: DriverOnboardingDocumentType): CaptureAssessment {
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
    val hasDocumentEdges = if (type == DriverOnboardingDocumentType.Selfie) {
        true
    } else {
        detectDocumentEdges(
            frameWidth = width,
            frameHeight = height,
            yAt = ::yAt,
        )
    }

    return when {
        brightness < 55 -> CaptureAssessment(false, "Move to a brighter area.", 0f)
        brightness > 220 -> CaptureAssessment(false, "Reduce glare on the ${type.shortLabel()}.", 0f)
        sharpness < 7 -> CaptureAssessment(false, "Hold steady and keep the ${type.shortLabel()} sharp.", 0f)
        !hasDocumentEdges -> CaptureAssessment(false, "Place all ${type.shortLabel()} edges inside the guide.", 0f)
        else -> CaptureAssessment(true, "Good. Hold steady for auto capture.", 0f)
    }
}

private fun assessSelfieFace(
    faces: List<Face>,
    frameWidth: Int,
    frameHeight: Int,
    applyPreviewAlignmentCorrection: Boolean,
): CaptureAssessment {
    val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
        ?: return CaptureAssessment(false, "Place your face inside the oval.", 0f)

    val box = face.boundingBox
    val faceCenterX = box.centerX().toFloat()
    val faceCenterY = box.centerY().toFloat()
    val guideBounds = guideBoundsFor(
        type = DriverOnboardingDocumentType.Selfie,
        frameWidth = frameWidth.toFloat(),
        frameHeight = frameHeight.toFloat(),
        selfieVerticalShiftFraction = if (applyPreviewAlignmentCorrection) 0.09f else 0f,
    )
    val guideHalfWidth = guideBounds.size.width / 2f
    val guideHalfHeight = guideBounds.size.height / 2f
    val normalizedCenterDistance = kotlin.math.hypot(
        (faceCenterX - guideBounds.centerX) / guideHalfWidth,
        (faceCenterY - guideBounds.centerY) / guideHalfHeight,
    )
    val faceWidthRatio = box.width().toFloat() / frameWidth.toFloat()
    val faceHeightRatio = box.height().toFloat() / frameHeight.toFloat()
    val landmarkSet = face.selfieLandmarkSet()

    return when {
        faces.size > 1 -> CaptureAssessment(false, "Only one face should be in the frame.", 0f)
        faceWidthRatio < 0.08f || faceHeightRatio < 0.08f -> CaptureAssessment(false, "Move closer to the camera.", 0f)
        !landmarkSet.hasRequiredFacePoints -> CaptureAssessment(false, "Keep your full face clearly visible.", 0f)
        !face.isStraightSelfiePose() -> CaptureAssessment(false, "Hold the camera straight at face level.", 0f)
        normalizedCenterDistance > 0.50f -> CaptureAssessment(false, "Center your face inside the oval.", 0f)
        !landmarkSet.requiredPointsInside(guideBounds) -> CaptureAssessment(false, "Move your full face inside the oval.", 0f)
        !landmarkSet.estimatedFaceInside(guideBounds) -> CaptureAssessment(false, "Move your full face inside the oval.", 0f)
        else -> CaptureAssessment(true, "Face detected. Hold steady for auto capture.", 0f)
    }
}

private fun Face.isStraightSelfiePose(): Boolean {
    val pitch = headEulerAngleX
    val yaw = headEulerAngleY
    val roll = headEulerAngleZ
    return kotlin.math.abs(pitch) <= 6f &&
        kotlin.math.abs(yaw) <= 14f &&
        kotlin.math.abs(roll) <= 10f
}

private data class SelfieLandmarkSet(
    val leftEye: PointF?,
    val rightEye: PointF?,
    val nose: PointF?,
    val mouthLeft: PointF?,
    val mouthRight: PointF?,
    val mouthBottom: PointF?,
) {
    val hasRequiredFacePoints: Boolean
        get() = leftEye != null &&
            rightEye != null &&
            nose != null &&
            (mouthBottom != null || (mouthLeft != null && mouthRight != null))

    fun requiredPointsInside(guideBounds: CaptureGuideBounds): Boolean {
        if (!hasRequiredFacePoints) return false
        return requiredPoints().all { it.isInsideOval(guideBounds, radiusScale = 1.0f) }
    }

    fun estimatedFaceInside(guideBounds: CaptureGuideBounds): Boolean {
        val leftEye = leftEye ?: return false
        val rightEye = rightEye ?: return false
        val lowerMouth = mouthBottom ?: averagePoint(mouthLeft ?: return false, mouthRight ?: return false)
        val eyeCenter = averagePoint(leftEye, rightEye)
        val eyeDistance = kotlin.math.abs(rightEye.x - leftEye.x).coerceAtLeast(1f)
        val eyeToMouth = kotlin.math.abs(lowerMouth.y - eyeCenter.y).coerceAtLeast(eyeDistance * 0.8f)
        val faceHalfWidth = eyeDistance * 0.92f
        val topY = eyeCenter.y - eyeToMouth * 0.68f
        val lowerY = lowerMouth.y + eyeToMouth * 0.18f
        val midY = eyeCenter.y + eyeToMouth * 0.30f
        val points = listOf(
            PointF(eyeCenter.x, topY),
            PointF(eyeCenter.x, lowerY),
            PointF(eyeCenter.x - faceHalfWidth, midY),
            PointF(eyeCenter.x + faceHalfWidth, midY),
            PointF(eyeCenter.x - faceHalfWidth * 0.72f, topY + eyeToMouth * 0.18f),
            PointF(eyeCenter.x + faceHalfWidth * 0.72f, topY + eyeToMouth * 0.18f),
            PointF(eyeCenter.x - faceHalfWidth * 0.68f, lowerY - eyeToMouth * 0.14f),
            PointF(eyeCenter.x + faceHalfWidth * 0.68f, lowerY - eyeToMouth * 0.14f),
        )
        return points.all { it.isInsideOval(guideBounds, radiusScale = 1.02f) }
    }

    private fun requiredPoints(): List<PointF> = listOfNotNull(
        leftEye,
        rightEye,
        nose,
        mouthLeft,
        mouthRight,
        mouthBottom,
    )
}

private fun Face.selfieLandmarkSet(): SelfieLandmarkSet {
    return SelfieLandmarkSet(
        leftEye = getLandmark(FaceLandmark.LEFT_EYE)?.position,
        rightEye = getLandmark(FaceLandmark.RIGHT_EYE)?.position,
        nose = getLandmark(FaceLandmark.NOSE_BASE)?.position,
        mouthLeft = getLandmark(FaceLandmark.MOUTH_LEFT)?.position,
        mouthRight = getLandmark(FaceLandmark.MOUTH_RIGHT)?.position,
        mouthBottom = getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position,
    )
}

private fun averagePoint(first: PointF, second: PointF): PointF {
    return PointF((first.x + second.x) / 2f, (first.y + second.y) / 2f)
}

private fun PointF.isInsideOval(
    guideBounds: CaptureGuideBounds,
    radiusScale: Float,
): Boolean {
    val normalizedX = (x - guideBounds.centerX) / ((guideBounds.size.width / 2f) * radiusScale)
    val normalizedY = (y - guideBounds.centerY) / ((guideBounds.size.height / 2f) * radiusScale)
    return normalizedX * normalizedX + normalizedY * normalizedY <= 1f
}

private fun detectDocumentEdges(
    frameWidth: Int,
    frameHeight: Int,
    yAt: (Int, Int) -> Int,
): Boolean {
    val left = frameWidth / 8
    val right = frameWidth - left
    val top = frameHeight / 5
    val bottom = frameHeight - top
    val scanStep = maxOf(4, minOf(frameWidth, frameHeight) / 96)
    val lineStep = scanStep * 3

    if (right - left < 96 || bottom - top < 72) return false

    fun verticalScore(x: Int): Int {
        var total = 0
        var count = 0
        var y = top
        while (y < bottom) {
            total += kotlin.math.abs(yAt(x + scanStep, y) - yAt(x - scanStep, y))
            count += 1
            y += lineStep
        }
        return if (count == 0) 0 else total / count
    }

    fun horizontalScore(y: Int): Int {
        var total = 0
        var count = 0
        var x = left
        while (x < right) {
            total += kotlin.math.abs(yAt(x, y + scanStep) - yAt(x, y - scanStep))
            count += 1
            x += lineStep
        }
        return if (count == 0) 0 else total / count
    }

    val midX = (left + right) / 2
    val midY = (top + bottom) / 2
    var leftPeak = 0
    var rightPeak = 0
    var verticalTotal = 0
    var verticalCount = 0
    var x = left + scanStep
    while (x < right - scanStep) {
        val score = verticalScore(x)
        verticalTotal += score
        verticalCount += 1
        if (x < midX) {
            leftPeak = maxOf(leftPeak, score)
        } else {
            rightPeak = maxOf(rightPeak, score)
        }
        x += scanStep
    }

    var topPeak = 0
    var bottomPeak = 0
    var horizontalTotal = 0
    var horizontalCount = 0
    var y = top + scanStep
    while (y < bottom - scanStep) {
        val score = horizontalScore(y)
        horizontalTotal += score
        horizontalCount += 1
        if (y < midY) {
            topPeak = maxOf(topPeak, score)
        } else {
            bottomPeak = maxOf(bottomPeak, score)
        }
        y += scanStep
    }

    val verticalAverage = if (verticalCount == 0) 0 else verticalTotal / verticalCount
    val horizontalAverage = if (horizontalCount == 0) 0 else horizontalTotal / horizontalCount
    val verticalThreshold = maxOf(18, verticalAverage * 2)
    val horizontalThreshold = maxOf(18, horizontalAverage * 2)

    return leftPeak >= verticalThreshold &&
        rightPeak >= verticalThreshold &&
        topPeak >= horizontalThreshold &&
        bottomPeak >= horizontalThreshold
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
