package org.noztek.esktransport.feature.driver.onboarding.presentation

import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreVideo.CVPixelBufferRef
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSProcessInfo
import platform.Vision.VNDetectFaceRectanglesRequest
import platform.Vision.VNFaceObservation
import platform.Vision.VNImageRequestHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Camera
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

private const val SelfieStableCaptureDurationMs = 700L
private const val SelfieDetectionGraceDurationMs = 420L

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DriverDocumentCaptureScreen(
    type: DriverOnboardingDocumentType,
    onCaptured: (CapturedDocumentImage) -> Unit,
    onClose: () -> Unit,
) {
    if (!type.isIdentityCapture()) {
        IosCaptureUnavailableContent(onClose = onClose)
        return
    }

    val isSelfie = type == DriverOnboardingDocumentType.Selfie
    val latestOnCaptured by androidx.compose.runtime.rememberUpdatedState(onCaptured)
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionResolved by remember { mutableStateOf(false) }
    var assessment by remember { mutableStateOf(IosCaptureAssessment(false, type.initialPrompt())) }
    var isCapturing by remember { mutableStateOf(false) }

    val camera = remember(type) {
        IosIdentityCameraController(
            type = type,
            onPermissionChanged = { granted ->
                permissionGranted = granted
                permissionResolved = true
            },
            onSelfieAssessmentChanged = { assessment = it },
            onCaptureStarted = { isCapturing = true },
            onCaptured = { image -> latestOnCaptured(image) },
            onCaptureFailed = { message ->
                isCapturing = false
                assessment = IosCaptureAssessment(false, message)
            },
        )
    }

    LaunchedEffect(camera) {
        camera.requestPermissionAndStart()
    }
    DisposableEffect(camera) {
        onDispose(camera::stop)
    }

    if (!permissionResolved || !permissionGranted) {
        IosCameraPermissionContent(
            isCheckingPermission = !permissionResolved,
            onRequestPermission = camera::requestPermissionAndStart,
            onClose = onClose,
        )
        return
    }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            UIKitView(
                modifier = Modifier.fillMaxSize(),
                factory = { camera.previewView },
            )
            if (isSelfie) {
                IosSelfieGuideOverlay(isGood = assessment.isGood)
            } else {
                IosLicenseGuideOverlay()
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            if (isSelfie) {
                IosSelfieStatusPanel(
                    assessment = assessment,
                    isCapturing = isCapturing,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            } else {
                IosManualCapturePanel(
                    isCapturing = isCapturing,
                    onCapture = camera::captureManually,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun IosLicenseGuideOverlay() {
    val overlayColor = Color(0xFF2563EB).copy(alpha = 0.70f)
    val guideColor = Color(0xFFBFDBFE)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val guideWidth = size.width * 0.90f
        val guideHeight = (guideWidth * 0.62f).coerceAtMost(size.height * 0.48f)
        val guideRect = Rect(
            offset = Offset(
                x = (size.width - guideWidth) / 2f,
                y = (size.height - guideHeight) / 2f - size.height * 0.04f,
            ),
            size = Size(guideWidth, guideHeight),
        )
        val overlay = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            addRect(guideRect)
        }
        drawPath(overlay, overlayColor)
        drawRect(
            color = guideColor,
            topLeft = guideRect.topLeft,
            size = guideRect.size,
            style = Stroke(width = 4f),
        )
    }
}

@Composable
private fun IosSelfieGuideOverlay(isGood: Boolean) {
    val overlayColor = if (isGood) Color(0xFF16A34A).copy(alpha = 0.70f) else Color(0xFF2563EB).copy(alpha = 0.70f)
    val guideColor = if (isGood) Color(0xFF22C55E) else Color(0xFFBFDBFE)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val guideWidth = size.width * 0.72f
        val guideHeight = (guideWidth * 1.28f).coerceAtMost(size.height * 0.58f)
        val guideRect = Rect(
            offset = Offset(
                x = (size.width - guideWidth) / 2f,
                y = (size.height - guideHeight) / 2f - size.height * 0.04f,
            ),
            size = Size(guideWidth, guideHeight),
        )
        val overlay = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            addOval(guideRect)
        }
        drawPath(overlay, overlayColor)
        drawOval(
            color = guideColor,
            topLeft = guideRect.topLeft,
            size = guideRect.size,
            style = Stroke(width = 5f),
        )
    }
}

@Composable
private fun IosManualCapturePanel(
    isCapturing: Boolean,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color.Black.copy(alpha = 0.72f),
        contentColor = Color.White,
    ) {
        Button(
            onClick = onCapture,
            enabled = !isCapturing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.Camera,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = if (isCapturing) "Capturing..." else "Capture license",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun IosSelfieStatusPanel(
    assessment: IosCaptureAssessment,
    isCapturing: Boolean,
    modifier: Modifier = Modifier,
) {
    val statusColor = if (assessment.isGood) Color(0xFF22C55E) else Color(0xFF93C5FD)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color.Black.copy(alpha = 0.72f),
        contentColor = Color.White,
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(modifier = Modifier.size(9.dp)) { drawCircle(statusColor) }
            Text(
                text = if (isCapturing) "Capturing..." else assessment.message,
                modifier = Modifier.padding(start = 10.dp).weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

private fun DriverOnboardingDocumentType.isIdentityCapture(): Boolean = when (this) {
    DriverOnboardingDocumentType.LicenseFront,
    DriverOnboardingDocumentType.LicenseBack,
    DriverOnboardingDocumentType.Selfie,
    -> true
    else -> false
}

private fun DriverOnboardingDocumentType.initialPrompt(): String = when (this) {
    DriverOnboardingDocumentType.Selfie -> "Place your face inside the oval."
    else -> "Position the entire license inside the frame."
}

@Composable
private fun IosCaptureUnavailableContent(onClose: () -> Unit) {
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
                text = "Vehicle capture is not available on iOS yet.",
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(onClick = onClose) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun IosCameraPermissionContent(
    isCheckingPermission: Boolean,
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
            if (isCheckingPermission) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Camera permission is required.",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Use the camera to capture your driver license and selfie inside the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = onRequestPermission) {
                    Text("Allow camera")
                }
                OutlinedButton(onClick = onClose) {
                    Text("Cancel")
                }
            }
        }
    }
}

private data class IosCaptureAssessment(
    val isGood: Boolean,
    val message: String,
)

private data class IosNormalizedCrop(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private class IosIdentityCameraController(
    private val type: DriverOnboardingDocumentType,
    private val onPermissionChanged: (Boolean) -> Unit,
    private val onSelfieAssessmentChanged: (IosCaptureAssessment) -> Unit,
    private val onCaptureStarted: () -> Unit,
    private val onCaptured: (CapturedDocumentImage) -> Unit,
    private val onCaptureFailed: (String) -> Unit,
) : NSObject() {
    private val isSelfie = type == DriverOnboardingDocumentType.Selfie
    private val session = AVCaptureSession()
    private val photoOutput = AVCapturePhotoOutput()
    private val videoOutput = AVCaptureVideoDataOutput()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)
    private var stableSinceMs: Long? = null
    private var lastSelfieGoodAtMs = 0L
    private var isCapturing = false
    private var photoDelegate: IosPhotoCaptureDelegate? = null
    private var selfieFrameAnalyzer: IosSelfieFrameAnalyzer? = null

    val previewView = IosCameraPreviewView(previewLayer)

    fun requestPermissionAndStart() {
        when (AVCaptureDevice.Companion.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                onPermissionChanged(true)
                start()
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.Companion.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        onPermissionChanged(granted)
                        if (granted) start()
                    }
                }
            }
            else -> onPermissionChanged(false)
        }
    }

    private fun start() {
        if (session.isRunning()) return
        val position = if (isSelfie) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        val device = AVCaptureDevice.Companion.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVMediaTypeVideo,
            position,
        ) ?: run {
            onCaptureFailed("This device does not have a usable camera.")
            return
        }
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null) ?: run {
            onCaptureFailed("Unable to start the camera.")
            return
        }

        session.beginConfiguration()
        session.sessionPreset = AVCaptureSessionPresetPhoto
        if (session.canAddInput(input)) session.addInput(input)
        if (session.canAddOutput(photoOutput)) session.addOutput(photoOutput)
        if (isSelfie && session.canAddOutput(videoOutput)) session.addOutput(videoOutput)
        session.commitConfiguration()

        if (isSelfie) {
            val analyzer = IosSelfieFrameAnalyzer(onAssessment = ::handleSelfieAssessment)
            selfieFrameAnalyzer = analyzer
            videoOutput.setSampleBufferDelegate(
                analyzer,
                queue = dispatch_get_global_queue(0, 0u),
            )
            videoOutput.connectionWithMediaType(AVMediaTypeVideo)?.let { connection ->
                if (connection.isVideoOrientationSupported()) {
                    connection.videoOrientation = AVCaptureVideoOrientationPortrait
                }
            }
        }
        photoOutput.connectionWithMediaType(AVMediaTypeVideo)?.let { connection ->
            if (connection.isVideoOrientationSupported()) {
                connection.videoOrientation = AVCaptureVideoOrientationPortrait
            }
        }
        session.startRunning()
    }

    fun captureManually() {
        if (!isSelfie) capturePhoto(licenseGuideCrop = previewView.licenseGuideCrop())
    }

    private fun handleSelfieAssessment(assessment: IosCaptureAssessment) {
        val now = (NSProcessInfo.processInfo.systemUptime * 1000.0).toLong()
        dispatch_async(dispatch_get_main_queue()) {
            if (isCapturing) return@dispatch_async
            val effectiveAssessment = when {
                assessment.isGood -> {
                    lastSelfieGoodAtMs = now
                    assessment
                }
                stableSinceMs != null && now - lastSelfieGoodAtMs <= SelfieDetectionGraceDurationMs -> {
                    IosCaptureAssessment(true, "Face detected. Hold steady for auto capture.")
                }
                else -> assessment
            }
            onSelfieAssessmentChanged(effectiveAssessment)
            stableSinceMs = if (effectiveAssessment.isGood) stableSinceMs ?: now else null
            if (effectiveAssessment.isGood && now - (stableSinceMs ?: now) >= SelfieStableCaptureDurationMs) {
                capturePhoto()
            }
        }
    }

    private fun capturePhoto(licenseGuideCrop: IosNormalizedCrop? = null) {
        if (isCapturing) return
        isCapturing = true
        onCaptureStarted()
        val delegate = IosPhotoCaptureDelegate(
            type = type,
            licenseGuideCrop = licenseGuideCrop,
            onSuccess = { image ->
                isCapturing = false
                photoDelegate = null
                onCaptured(image)
            },
            onFailure = { message ->
                isCapturing = false
                photoDelegate = null
                stableSinceMs = null
                lastSelfieGoodAtMs = 0L
                onCaptureFailed(message)
            },
        )
        photoDelegate = delegate
        photoOutput.capturePhotoWithSettings(AVCapturePhotoSettings.photoSettings(), delegate)
    }

    fun stop() {
        videoOutput.setSampleBufferDelegate(null, queue = null)
        selfieFrameAnalyzer = null
        if (session.isRunning()) session.stopRunning()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosCameraPreviewView(previewLayer: AVCaptureVideoPreviewLayer) : UIView(
    frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
) {
    private val cameraPreviewLayer = previewLayer

    init {
        backgroundColor = UIColor.blackColor
        cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(cameraPreviewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        cameraPreviewLayer.frame = bounds
    }

    fun licenseGuideCrop(): IosNormalizedCrop {
        val guideRect = bounds.useContents {
            val guideWidth = size.width * 0.90
            val guideHeight = minOf(guideWidth * 0.62, size.height * 0.48)
            CGRectMake(
                x = (size.width - guideWidth) / 2.0,
                y = (size.height - guideHeight) / 2.0 - size.height * 0.04,
                width = guideWidth,
                height = guideHeight,
            )
        }
        return cameraPreviewLayer.metadataOutputRectOfInterestForRect(guideRect).useContents {
            IosNormalizedCrop(
                x = origin.x,
                y = origin.y,
                width = size.width,
                height = size.height,
            )
        }
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private class IosSelfieFrameAnalyzer(
    private val onAssessment: (IosCaptureAssessment) -> Unit,
) : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
    private var lastAnalysisMs = 0L

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection,
    ) {
        val now = (NSProcessInfo.processInfo.systemUptime * 1000.0).toLong()
        if (now - lastAnalysisMs < 180L) return
        lastAnalysisMs = now

        val imageBuffer = didOutputSampleBuffer?.let(::CMSampleBufferGetImageBuffer) ?: return
        val request = VNDetectFaceRectanglesRequest()
        val handler = VNImageRequestHandler(imageBuffer, options = emptyMap<Any?, Any>())
        if (!handler.performRequests(listOf(request), error = null)) {
            onAssessment(IosCaptureAssessment(false, "Face detection is not ready. Try again."))
            return
        }
        val faces = request.results?.filterIsInstance<VNFaceObservation>().orEmpty()
        val face = faces.maxByOrNull { it.boundingBox.useContents { size.width * size.height } }
        if (face == null) {
            onAssessment(IosCaptureAssessment(false, "Place your face inside the oval."))
            return
        }
        if (faces.size > 1) {
            onAssessment(IosCaptureAssessment(false, "Only one face should be in the frame."))
            return
        }
        val isCentered = face.boundingBox.useContents {
            val centerX = origin.x + size.width / 2.0
            val centerY = origin.y + size.height / 2.0
            val acceptableWidth = size.width in 0.12..0.60
            val acceptableHeight = size.height in 0.14..0.66
            acceptableWidth && acceptableHeight &&
                kotlin.math.abs(centerX - 0.5) < 0.20 &&
                kotlin.math.abs(centerY - 0.5) < 0.20
        }
        onAssessment(
            if (isCentered) {
                IosCaptureAssessment(true, "Face detected. Hold steady for auto capture.")
            } else {
                IosCaptureAssessment(false, "Center your full face inside the oval.")
            },
        )
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private class IosPhotoCaptureDelegate(
    private val type: DriverOnboardingDocumentType,
    private val licenseGuideCrop: IosNormalizedCrop?,
    private val onSuccess: (CapturedDocumentImage) -> Unit,
    private val onFailure: (String) -> Unit,
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        val data = didFinishProcessingPhoto.fileDataRepresentation()
        if (error != null || data == null) {
            dispatch_async(dispatch_get_main_queue()) {
                onFailure("Capture failed. Hold steady and try again.")
            }
            return
        }
        val capturedData = when {
            type.isLicenseCapture() && licenseGuideCrop != null -> cropLicenseGuide(data, licenseGuideCrop)
            type == DriverOnboardingDocumentType.Selfie -> cropSelfieToFace(data)
            else -> data
        }
        dispatch_async(dispatch_get_main_queue()) {
            onSuccess(
                CapturedDocumentImage(
                    fileName = "driver_${type.apiValue}_${(NSProcessInfo.processInfo.systemUptime * 1000.0).toLong()}.jpg",
                    mimeType = "image/jpeg",
                    bytes = capturedData.toByteArray(),
                ),
            )
        }
    }
}

private fun DriverOnboardingDocumentType.isLicenseCapture(): Boolean =
    this == DriverOnboardingDocumentType.LicenseFront || this == DriverOnboardingDocumentType.LicenseBack

@OptIn(ExperimentalForeignApi::class)
private fun cropLicenseGuide(data: NSData, guide: IosNormalizedCrop): NSData {
    val sourceImage = UIImage(data = data)
    val cgImage = sourceImage.CGImage ?: return data
    val width = CGImageGetWidth(cgImage).toDouble()
    val height = CGImageGetHeight(cgImage).toDouble()
    val cropX = (guide.x * width).coerceIn(0.0, width - 1.0)
    val cropY = (guide.y * height).coerceIn(0.0, height - 1.0)
    val cropWidth = (guide.width * width).coerceIn(1.0, width - cropX)
    val cropHeight = (guide.height * height).coerceIn(1.0, height - cropY)
    val crop = CGRectMake(
        x = cropX,
        y = cropY,
        width = cropWidth,
        height = cropHeight,
    )
    val croppedImage = CGImageCreateWithImageInRect(cgImage, crop) ?: return data
    val orientedCrop = UIImage(
        cGImage = croppedImage,
        scale = sourceImage.scale,
        orientation = sourceImage.imageOrientation,
    )
    return UIImageJPEGRepresentation(orientedCrop, 0.98) ?: data
}

private data class IosNormalizedPoint(
    val x: Double,
    val y: Double,
)

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private fun cropSelfieToFace(data: NSData): NSData {
    val sourceImage = UIImage(data = data)
    val cgImage = sourceImage.CGImage ?: return data
    val request = VNDetectFaceRectanglesRequest()
    val handler = VNImageRequestHandler(data, options = emptyMap<Any?, Any>())
    if (!handler.performRequests(listOf(request), error = null)) return data

    val face = request.results
        ?.filterIsInstance<VNFaceObservation>()
        ?.maxByOrNull { observation ->
            observation.boundingBox.useContents { size.width * size.height }
        }
        ?: return data
    val faceBounds = face.boundingBox.useContents {
        listOf(
            IosNormalizedPoint(origin.x, 1.0 - origin.y - size.height),
            IosNormalizedPoint(origin.x + size.width, 1.0 - origin.y - size.height),
            IosNormalizedPoint(origin.x + size.width, 1.0 - origin.y),
            IosNormalizedPoint(origin.x, 1.0 - origin.y),
        )
    }.map(sourceImage::uprightPointToRaw)
    val minX = faceBounds.minOf { it.x }
    val maxX = faceBounds.maxOf { it.x }
    val minY = faceBounds.minOf { it.y }
    val maxY = faceBounds.maxOf { it.y }
    val faceWidth = (maxX - minX).coerceAtLeast(0.01)
    val faceHeight = (maxY - minY).coerceAtLeast(0.01)
    val left = (minX - faceWidth * 0.28).coerceIn(0.0, 1.0)
    val top = (minY - faceHeight * 0.34).coerceIn(0.0, 1.0)
    val right = (maxX + faceWidth * 0.28).coerceIn(left + 0.01, 1.0)
    val bottom = (maxY + faceHeight * 0.30).coerceIn(top + 0.01, 1.0)
    val crop = CGRectMake(
        x = left * CGImageGetWidth(cgImage).toDouble(),
        y = top * CGImageGetHeight(cgImage).toDouble(),
        width = (right - left) * CGImageGetWidth(cgImage).toDouble(),
        height = (bottom - top) * CGImageGetHeight(cgImage).toDouble(),
    )
    val croppedImage = CGImageCreateWithImageInRect(cgImage, crop) ?: return data
    val orientedCrop = UIImage(
        cGImage = croppedImage,
        scale = sourceImage.scale,
        orientation = sourceImage.imageOrientation,
    )
    return UIImageJPEGRepresentation(orientedCrop, 0.98) ?: data
}

private fun UIImage.uprightPointToRaw(point: IosNormalizedPoint): IosNormalizedPoint {
    return when (imageOrientation.name) {
        "UIImageOrientationUpMirrored" -> IosNormalizedPoint(1.0 - point.x, point.y)
        "UIImageOrientationDown" -> IosNormalizedPoint(1.0 - point.x, 1.0 - point.y)
        "UIImageOrientationDownMirrored" -> IosNormalizedPoint(point.x, 1.0 - point.y)
        "UIImageOrientationLeftMirrored" -> IosNormalizedPoint(point.y, point.x)
        "UIImageOrientationRight" -> IosNormalizedPoint(point.y, 1.0 - point.x)
        "UIImageOrientationRightMirrored" -> IosNormalizedPoint(1.0 - point.y, 1.0 - point.x)
        "UIImageOrientationLeft" -> IosNormalizedPoint(1.0 - point.y, point.x)
        else -> point
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    result.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), bytes, length)
    }
    return result
}
