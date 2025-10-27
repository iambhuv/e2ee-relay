@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
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
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar


@Composable
fun QrScannerScreen(activity: QrScanner) {
    Box(
        Modifier.fillMaxSize()
    ) {
        val cameraPermission by activity.cameraPermissionState
        val cameraProvider by activity.cameraProviderState

        cameraProvider?.let {
            CameraPreview(it, activity.imageAnalysis, Modifier.fillMaxWidth())
        }

        Box(contentAlignment = Alignment.Center) {
            if (cameraPermission != PermissionState.Granted) {
                activity.requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                ScannerUI(activity)
            }
        }

        BackTopBar("Scan QR")
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ScannerUI(activity: QrScanner) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        activity.barcodeScanner = BarcodeScanning.getClient()
        activity.imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(context), activity.analyzer
        )
        activity.cameraProviderFuture.addListener({
            activity.cameraProviderState.value = activity.cameraProviderFuture.get()
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
private fun CameraPreview(
    cameraProvider: ProcessCameraProvider, imageAnalysis: ImageAnalysis, modifier: Modifier
) {
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