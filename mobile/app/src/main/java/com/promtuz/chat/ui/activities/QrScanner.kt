package com.promtuz.chat.ui.activities

import android.Manifest
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
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.theme.PromtuzTheme


@ExperimentalGetImage
class QrScanner : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var barcodeScanner: BarcodeScanner

    private val cameraPermissionState = mutableStateOf(PermissionState.NotRequested)
    private val cameraProviderState = mutableStateOf<ProcessCameraProvider?>(null)

    private var requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                cameraPermissionState.value = PermissionState.Granted
            } else {
                cameraPermissionState.value = PermissionState.Denied
            }
        }

    override fun onStart() {
        super.onStart()

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
                Scaffold(
                    Modifier.fillMaxSize(),
                    topBar = {
                        BackTopBar { Text("Scan QR") }
                    }
                ) { _ ->
                    val cameraPermission by cameraPermissionState
                    val cameraProvider by cameraProviderState

                    Box(Modifier.fillMaxSize()) {
                        cameraProvider?.let {
                            CameraPreview(it, Modifier.fillMaxSize())
                        }

                        Box(contentAlignment = Alignment.Center) {
                            if (cameraPermission != PermissionState.Granted) {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                ScannerUI()
                            }
                        }
                    }
                }
            }
        }
    }

    private val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        val inputImage = InputImage.fromMediaImage(imageProxy.image ?: return@Analyzer, 90)

        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val qr = barcodes.first()
                qr ?: return@addOnSuccessListener

                qr.rawBytes?.let {
                    // onSuccess(it)
                    setResult(RESULT_OK, Intent().putExtra("qr_result", it))
                    finish()
                }
            }
        }.addOnFailureListener { exception ->
            Log.d("QrScanner", "Scan Fail: ", exception)
            setResult(RESULT_CANCELED, Intent().putExtra("exception", exception))
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ScannerUI() {
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            barcodeScanner = BarcodeScanning.getClient()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
            cameraProviderFuture.addListener({
                cameraProviderState.value = cameraProviderFuture.get()
            }, ContextCompat.getMainExecutor(context))
        }

        val scanSize = with(LocalDensity.current) { 225.dp.toPx() }
        val cornerRadius = with(LocalDensity.current) { 25.dp.toPx() }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val left = (size.width - scanSize) / 2
            val top = (size.height - scanSize) / 2

            drawRect(
                color = Color.Black.copy(alpha = 0.7f)
            )

            drawRoundRect(
                Color.Transparent,
                topLeft = Offset(
                    left, top
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                size = Size(scanSize, scanSize),
                blendMode = BlendMode.Clear
            )
        }
    }

    @Composable
    private fun CameraPreview(cameraProvider: ProcessCameraProvider, modifier: Modifier) {
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(factory = { context ->
            val previewView = PreviewView(context)
            val preview: Preview = Preview.Builder().build()
            val cameraSelector: CameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            preview.surfaceProvider = previewView.surfaceProvider

            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis, preview)

            previewView
        }, modifier)
    }
}