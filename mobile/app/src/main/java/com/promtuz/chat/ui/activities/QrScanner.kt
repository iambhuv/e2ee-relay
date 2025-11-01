package com.promtuz.chat.ui.activities

import android.Manifest
import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.*
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.presentation.viewmodel.QrScannerVM
import com.promtuz.chat.ui.screens.QrScannerScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.utils.common.OneEuroFilter2D
import com.promtuz.chat.utils.extensions.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.math.max

@ExperimentalGetImage
class QrScanner : AppCompatActivity() {
    private val qrScannerVM: QrScannerVM by inject<QrScannerVM>()

    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var imageAnalysis: ImageAnalysis
    lateinit var barcodeScanner: BarcodeScanner
    lateinit var camera: Camera

    val isCameraAvailable = mutableStateOf(false)
    val cameraPermissionState = mutableStateOf(PermissionState.NotRequested)
    val cameraProviderState = mutableStateOf<ProcessCameraProvider?>(null)
    val viewSize = mutableStateOf(Size.Zero)

    var requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraPermissionState.value = PermissionState.Granted
        } else {
            cameraPermissionState.value = PermissionState.Denied
        }
    }

    private val smootherMap = mutableMapOf<String, OneEuroFilter2D>()
    private fun mapImageToView(
        id: String,
        rect: Rect,
        imageProxy: ImageProxy,
    ): RectF {
        val (viewWidth, viewHeight) = viewSize.value
        val rotation = imageProxy.imageInfo.rotationDegrees
        val (imgW, imgH) = if (rotation % 180 == 0) imageProxy.width.toFloat() to imageProxy.height.toFloat()
        else imageProxy.height.toFloat() to imageProxy.width.toFloat()

        val scale = max(viewWidth / imgW, viewHeight / imgH)

        val scaledImageWidth = imgW * scale
        val scaledImageHeight = imgH * scale

        // amount cropped on each side in *view* pixels, convert back to image-space by dividing by scale
        val xOffsetImageSpace = ((scaledImageWidth - viewWidth) / 2f).coerceAtLeast(0f) / scale
        val yOffsetImageSpace = ((scaledImageHeight - viewHeight) / 2f).coerceAtLeast(0f) / scale

        // map rect from image-space -> view-space (apply offset then scale)
        val left = (rect.left - xOffsetImageSpace) * scale
        val top = (rect.top - yOffsetImageSpace) * scale
        val right = left + (rect.width() * scale)
        val bottom = top + (rect.height() * scale)

        val map = RectF(left, top, right, bottom)
        val smoother = smootherMap.getOrPut(id) { OneEuroFilter2D() }
        return smoother.filter(map)
    }

    val expectsResult: Boolean by lazy {
        callingActivity != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PromtuzTheme {
                QrScannerScreen(this)
            }
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun initScanner() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val scannerOptions =
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .setExecutor(ContextCompat.getMainExecutor(this))
                .setZoomSuggestionOptions(
                    ZoomSuggestionOptions.Builder { suggestedZoom ->
                        val control = camera.cameraControl
                        val info = camera.cameraInfo
                        val current = info.zoomState.value?.zoomRatio ?: 1f
                        val target = suggestedZoom
                        val steps = 20
                        val diff = (target - current) / steps

                        CoroutineScope(Dispatchers.Main).launch {
                            repeat(steps) {
                                control.setZoomRatio(current + diff * (it + 1))
                                delay(16)
                            }
                        }
                        true
                    }.setMaxSupportedZoomRatio(1.5f).build()
                ).build()

        barcodeScanner = BarcodeScanning.getClient(scannerOptions)


        cameraProviderFuture.addListener({
            cameraProviderState.value = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(this))

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this), qrAnalyzer()
        )
    }

    private var isScanning = false

    /**
     * FIXME:
     *  Scanner is unaware of screen's rotation
     */
    private fun qrAnalyzer() = ImageAnalysis.Analyzer { imageProxy ->
        isScanning.then { return@Analyzer imageProxy.close() }
        isScanning = true

        val inputImage = InputImage.fromMediaImage(imageProxy.image ?: return@Analyzer, 90)

        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                val bytes = barcode.rawBytes ?: continue
                val identity = Identity.fromByteArray(bytes) ?: continue

                // TODO
            }
        }.addOnFailureListener { exception ->
            Timber.tag("QrScanner").e(exception, "Scan Fail: ")

            expectsResult.then {
                setResult(RESULT_CANCELED, Intent().putExtra("exception", exception))
            }
        }.addOnCompleteListener {
            imageProxy.close()
            isScanning = false
        }
    }
}