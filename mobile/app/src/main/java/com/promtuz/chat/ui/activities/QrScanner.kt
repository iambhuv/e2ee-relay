package com.promtuz.chat.ui.activities

import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.*
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.screens.QrScannerScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.utils.extensions.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max


private const val QR_DETECT_THRESHOLD = 300L; // ms

data class TrackedQr(val id: String, var size: Size, var rect: RectF, var lastSeen: Long)

/**
 * TODO:
 *  This activity should scan all the qr's present on the camera view,
 *  validate them, all validated qr's should be parsed into parseable entities supported by app,
 *  parsed entities can be shown in the middle of the designated qr while tracking it,
 *  tracking the qr and identifying the entity with the qr on screen can be challenging,
 *  end product should look stunning though, people can instead of
 *  instantaneous scan decision, choose themself,
 *  so it would be sort of "AR", fallbacks should be made of course -
 *  for devices with less capable hardware
 *
 */
@ExperimentalGetImage
class QrScanner : AppCompatActivity() {
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var imageAnalysis: ImageAnalysis
    lateinit var barcodeScanner: BarcodeScanner
    lateinit var camera: Camera

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


    private var lastSeen = 0L
    private var lastEmpty = 0L
    val trackedQrCodes = mutableStateListOf<TrackedQr>()

    val analyzer = ImageAnalysis.Analyzer { imageProxy ->

        val inputImage = InputImage.fromMediaImage(imageProxy.image ?: return@Analyzer, 90)

        val (imgW, imgH) = if (imageProxy.imageInfo.rotationDegrees % 180 == 0) imageProxy.width to imageProxy.height
        else imageProxy.height to imageProxy.width

        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            var trackedQrList = mutableListOf<TrackedQr>()

            for (barcode in barcodes) {
                val rect = barcode.boundingBox ?: continue

                imageProxy.imageInfo.rotationDegrees - display.rotation

                val (vHeight, vWidth) = viewSize.value

                val mapped = mapImageToView(rect, imageProxy)
                val id = barcode.rawValue ?: "${rect.centerX()}:${rect.centerY()}"
                val scaleX = vWidth / imgW.toFloat()
                val scaleY = vHeight / imgH.toFloat()
                val scale = max(scaleX, scaleY) * 0.75f
                val qrWidth = rect.width() * scale
                val qrHeight = rect.height() * scale

                trackedQrList.add(
                    TrackedQr(
                        id, Size(qrWidth, qrHeight), mapped, SystemClock.elapsedRealtime()
                    )
                )
            }

            updateTrackedQrs(trackedQrList)
        }.addOnFailureListener { exception ->
            Log.d("QrScanner", "Scan Fail: ", exception)

            expectsResult.then {
                setResult(RESULT_CANCELED, Intent().putExtra("exception", exception))
            }
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    fun updateTrackedQrs(new: List<TrackedQr>) {
        val now = SystemClock.elapsedRealtime()

        // update or add
        new.forEach { qr ->
            val existing = trackedQrCodes.find { it.id == qr.id }
            if (existing != null) {
                existing.rect = qr.rect
                existing.size = qr.size
                existing.lastSeen = now
            } else {
                trackedQrCodes.add(qr.copy(lastSeen = now))
            }
        }

        // remove those unseen for > threshold
        trackedQrCodes.removeAll { now - it.lastSeen > QR_DETECT_THRESHOLD }
    }

    private fun <T> SnapshotStateList<T>.setAll(items: List<T>) {
        clear(); addAll(items)
    }

    private fun mapImageToView(
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

        return RectF(left, top, right, bottom)
    }

    private val expectsResult: Boolean by lazy {
        callingActivity != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val scannerOptions =
            BarcodeScannerOptions.Builder()
                // why bother with anything else
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .setExecutor(ContextCompat.getMainExecutor(this))
                .setZoomSuggestionOptions(
                    ZoomSuggestionOptions.Builder { suggestedZoom ->
                        Log.d("QrScanner", "TRACE: ZoomSuggested at $suggestedZoom")

                        val control = camera.cameraControl
                        val info = camera.cameraInfo
                        val current = info.zoomState.value?.zoomRatio ?: 1f
                        val target = suggestedZoom
                        val steps = 10
                        val diff = (target - current) / steps

                        CoroutineScope(Dispatchers.Main).launch {
                            repeat(steps) {
                                control.setZoomRatio(current + diff * (it + 1))
                                delay(8)
                            }
                        }
                        true
                    }.setMaxSupportedZoomRatio(10f).build()
                ).build()

        barcodeScanner = BarcodeScanning.getClient(scannerOptions)


        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this), analyzer
        )
        cameraProviderFuture.addListener({
            cameraProviderState.value = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(this))


        enableEdgeToEdge()
        setContent {
            PromtuzTheme {
                QrScannerScreen(this)
            }
        }
    }
}