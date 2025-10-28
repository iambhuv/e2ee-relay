@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
import android.os.SystemClock
import android.util.Rational
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.utils.extensions.then
import kotlinx.coroutines.delay


@Composable
fun QrScannerScreen(activity: QrScanner) {
    Box(
        Modifier.fillMaxSize()
    ) {
        val cameraPermission by activity.cameraPermissionState
        val cameraProvider by activity.cameraProviderState

        cameraProvider?.let {
            CameraPreview(
                it, activity.imageAnalysis, Modifier
                    .fillMaxSize()
                    .onSizeChanged { (w, h) ->
                        activity.viewSize.value = Size(w.toFloat(), h.toFloat())
                    })
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
private fun BoxScope.ScannerUI(activity: QrScanner) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

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
    val trackedQrCodes = activity.trackedQrCodes

    var prevSize by remember { mutableIntStateOf(0) }

    LaunchedEffect(trackedQrCodes.size) {
        (trackedQrCodes.size > prevSize).then {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        prevSize = trackedQrCodes.size
    }

    Text(
        trackedQrCodes.size.toString(),
        Modifier.align(Alignment.Center),
        style = MaterialTheme.typography.titleLargeEmphasized
    )

    val ticker = remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            ticker.value = SystemClock.elapsedRealtime()
            delay(16)
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        ticker.value

        val left = (size.width - scanSize) / 2
        val top = (size.height - scanSize) / 2

        for (qr in trackedQrCodes) {
            // qr.size.width
            drawRoundRect(
                Color.Red,
                topLeft = Offset(qr.rect.left, qr.rect.top),
                cornerRadius = CornerRadius(10F, 10F),
                style = Stroke(4F),
                size = qr.size
            )
        }

        drawRoundRect(
            Color.White,
            topLeft = Offset(
                left, top
            ),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(2f),
            size = Size(scanSize, scanSize)
        )
    }
}

@Composable
private fun CameraPreview(
    cameraProvider: ProcessCameraProvider, imageAnalysis: ImageAnalysis, modifier: Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }, update = { previewView ->
            previewView.doOnLayout {
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                preview.surfaceProvider = previewView.surfaceProvider

                val viewPort = ViewPort.Builder(
                    Rational(previewView.width, previewView.height), previewView.display.rotation
                ).build()
                viewPort.aspectRatio

                val useCaseGroup =
                    UseCaseGroup.Builder().addUseCase(preview).addUseCase(imageAnalysis)
                        .setViewPort(viewPort).build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
            }
        }, modifier = modifier
    )
}