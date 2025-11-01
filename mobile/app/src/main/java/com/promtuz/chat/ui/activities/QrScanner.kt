package com.promtuz.chat.ui.activities

import android.Manifest
import android.content.Intent
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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.presentation.viewmodel.QrScannerVM
import com.promtuz.chat.ui.screens.QrScannerScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.utils.extensions.then
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalGetImage
class QrScanner : AppCompatActivity() {
    private val viewModel: QrScannerVM by viewModel()

    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var barcodeScanner: BarcodeScanner
    lateinit var camera: Camera

    var requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.handleCameraPermissionRequest(it) }

    val expectsResult: Boolean by lazy {
        callingActivity != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PromtuzTheme {
                QrScannerScreen(this, viewModel)
            }
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    fun initScanner() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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
            viewModel.setCameraProvider(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))

        viewModel.imageAnalysis.setAnalyzer(
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
            val localIdentities = mutableSetOf<Identity>()
            for (barcode in barcodes) {
                val bytes = barcode.rawBytes ?: continue
                val identity = Identity.fromByteArray(bytes) ?: continue
                localIdentities.add(identity)
            }
            viewModel.refreshIdentities(localIdentities)
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