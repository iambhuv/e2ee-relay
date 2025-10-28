@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.utils.extensions.then


@Composable
fun QrScannerScreen(activity: QrScanner) {
    Box(
        Modifier.fillMaxSize()
    ) {
        val cameraPermission by activity.cameraPermissionState
        val cameraProvider by activity.cameraProviderState

        cameraProvider?.let {
            CameraPreview(
                activity, it, Modifier
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
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme

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
        color = colors.onBackground,
        style = MaterialTheme.typography.displayLargeEmphasized.copy(
            shadow = Shadow(
                colors.background,
                blurRadius = 6F,
            )
        ),
        fontWeight = FontWeight.Bold
    )

//    val ticker = remember { mutableLongStateOf(0L) }
//    LaunchedEffect(Unit) {
//        while (true) {
//            ticker.longValue = SystemClock.elapsedRealtime()
//            delay(1)
//        }
//    }

//    Canvas(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        ticker.longValue
//
//        val left = (size.width - scanSize) / 2
//        val top = (size.height - scanSize) / 2
//
//        for (qr in trackedQrCodes) {
//            drawRoundRect(
//                colors.primary,
//                topLeft = Offset(qr.rect.left, qr.rect.top),
//                cornerRadius = CornerRadius(10F, 10F),
//                style = Stroke(4F),
//                size = qr.size
//            )
//        }
//
//        drawRoundRect(
//            colors.onBackground,
//            topLeft = Offset(
//                left, top
//            ),
//            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
//            style = Stroke(2f),
//            size = Size(scanSize, scanSize)
//        )
//    }
}

@Composable
private fun CameraPreview(
    activity: QrScanner, cameraProvider: ProcessCameraProvider, modifier: Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                val previewView = PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val previewOverlay = object : View(context) {
                    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                        color = Color.GREEN
                    }

                    private val frameRunnable = object : Runnable {
                        override fun run() {
                            invalidate()
                            postOnAnimation(this)
                        }
                    }

                    init {
                        setWillNotDraw(false)
                    }

                    override fun onAttachedToWindow() {
                        super.onAttachedToWindow()
                        post(frameRunnable)
                    }

                    override fun onDetachedFromWindow() {
                        removeCallbacks(frameRunnable)
                        super.onDetachedFromWindow()
                    }


                    override fun onDraw(canvas: android.graphics.Canvas) {
                        super.onDraw(canvas)

                        for (qr in activity.trackedQrCodes) {
                            canvas.drawRoundRect(qr.rect, 20f, 20f, paint)
                        }
                    }
                }.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                addView(previewView)
                addView(previewOverlay)

                tag = previewView
            }
        }, update = { frameLayout ->
            val previewView = frameLayout.tag as PreviewView

            previewView.doOnLayout {
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                preview.surfaceProvider = previewView.surfaceProvider

                val viewPort = ViewPort.Builder(
                    Rational(previewView.width, previewView.height), previewView.display.rotation
                ).build()
                viewPort.aspectRatio

                val useCaseGroup =
                    UseCaseGroup.Builder().addUseCase(preview).addUseCase(activity.imageAnalysis)
                        .setViewPort(viewPort).build()

                cameraProvider.unbindAll()
                activity.camera =
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
            }
        }, modifier = modifier
    )
}