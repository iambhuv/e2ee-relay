package com.promtuz.chat.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.screens.QrScannerScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.utils.extensions.then


@ExperimentalGetImage
class QrScanner : AppCompatActivity() {
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var imageAnalysis: ImageAnalysis
    lateinit var barcodeScanner: BarcodeScanner

    val cameraPermissionState = mutableStateOf(PermissionState.NotRequested)
    val cameraProviderState = mutableStateOf<ProcessCameraProvider?>(null)

    var requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraPermissionState.value = PermissionState.Granted
        } else {
            cameraPermissionState.value = PermissionState.Denied
        }
    }

    val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        val inputImage = InputImage.fromMediaImage(imageProxy.image ?: return@Analyzer, 90)

        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val qr = barcodes.first()
                qr ?: return@addOnSuccessListener

                qr.rawBytes?.let { bytes ->
                    if (expectsResult) {
                        setResult(RESULT_OK, Intent().putExtra("qr_result", bytes))
                        finish()
                    } else {
                        /// DO SOMETHING!
                        /// LIKE?
                        /// SCAN AND PARSE MAYBE?
                        /// QR CODE MUST FOLLOW A STRUCT
                        /// OR ENUM?
                        /// ENUM CODE : DATA
                        /// NICE!
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.d("QrScanner", "Scan Fail: ", exception)

            expectsResult.then {
                setResult(RESULT_CANCELED, Intent().putExtra("exception", exception))
            }
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    private val expectsResult: Boolean by lazy {
        callingActivity != null
    }

    override fun onStart() {
        super.onStart()

        referrer

        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        WindowCompat.enableEdgeToEdge(window)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            false

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        return super.onCreateView(name, context, attrs)
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
}