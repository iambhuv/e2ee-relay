package com.promtuz.chat.ui.activities

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.promtuz.chat.R
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.theme.PromtuzTheme


class QrScanner(
    private val onSuccess: (value: ByteArray) -> Unit, private val onError: (e: Exception) -> Unit
) : BottomSheetDialogFragment() {
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {
            setOnShowListener {
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.isFitToContents = false // Important for full screen
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

            }

            cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            window?.setDimAmount(0f)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            WindowCompat.enableEdgeToEdge(it)
            WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightStatusBars = false

            WindowCompat.setDecorFitsSystemWindows(it, false)
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setPadding(0, 0, 0, 0)
        fitsSystemWindows = true

        setContent {
            PromtuzTheme {
                ConstraintLayout(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                ) {
                    val (closeIcon, wrapper) = createRefs()

                    val cameraPermission by cameraPermissionState
                    val cameraProvider by cameraProviderState

                    cameraProvider?.let {
                        CameraPreview(it, Modifier.fillMaxSize())
                    }

                    Box(Modifier.constrainAs(wrapper) {
                        centerTo(parent)
                    }) {
                        if (cameraPermission != PermissionState.Granted) {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            ScannerUI()
                        }
                    }

                    IconButton({
                        dismiss()
                    }, modifier = Modifier.constrainAs(closeIcon) {
                        top.linkTo(parent.top, margin = 16.dp)
                        absoluteRight.linkTo(parent.absoluteRight, margin = 16.dp)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.i_close),
                            "Close",
                            Modifier.size(28.dp),
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->
            bottomSheetDialog.behavior.apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                peekHeight = resources.displayMetrics.heightPixels
            }

            // Remove all padding and background from the bottom sheet container
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.apply {
                setPadding(0, 0, 0, 0)
            }

            // Remove padding from the coordinator layout parent
            val coordinator = bottomSheet?.parent as? View
            coordinator?.apply {
                setPadding(0, 0, 0, 0)
                fitsSystemWindows = false
            }
        }

        // Remove padding from the view itself
        view.setPadding(0, 0, 0, 0)
        (view.parent as? View)?.setPadding(0, 0, 0, 0)
    }

    private val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        val inputImage = InputImage.fromMediaImage(imageProxy.image ?: return@Analyzer, 90)

        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val qr = barcodes.first()
                qr ?: return@addOnSuccessListener

                qr.rawBytes?.let {
                    onSuccess(it)
                    dismiss()
                }
            }
        }.addOnFailureListener { exception ->
            Log.d("QrScanner", "Scan Fail: ", exception)
            onError(exception)
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