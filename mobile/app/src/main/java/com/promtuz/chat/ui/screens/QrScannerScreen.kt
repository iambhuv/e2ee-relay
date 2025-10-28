@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
import android.graphics.Color
import android.graphics.Paint
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.promtuz.chat.R
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.text.avgSizeInStyle


@Composable
fun QrScannerScreen(activity: QrScanner) {
    val textTheme = MaterialTheme.typography
    val backHandler = LocalOnBackPressedDispatcherOwner.current

    var torchEnabled by remember { mutableStateOf(false) }
    var haveCamera by activity.isCameraAvailable

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
            }
        }

        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            modifier = Modifier.background(Brush.verticalGradient(
                listOf(
                    androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                    androidx.compose.ui.graphics.Color.Transparent
                )
            )),
            navigationIcon = {
                IconButton({
                    backHandler?.onBackPressedDispatcher?.onBackPressed()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.i_back),
                        "Go Back",
                        Modifier.size(28.dp),
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }, title = {
                Text(
                    "Scan QR", style = avgSizeInStyle(
                        textTheme.titleLargeEmphasized, textTheme.titleMediumEmphasized
                    )
                )
            },
            actions = {
                if (haveCamera) {
                    IconButton({
                        torchEnabled = !torchEnabled
                        activity.camera.cameraControl.enableTorch(torchEnabled)
                    }) {
                        Icon(
                            painter = if (torchEnabled) painterResource(R.drawable.i_flash_off) else painterResource(
                                R.drawable.i_flash_on
                            ),
                            if (torchEnabled) "Turn Flash Off" else "Turn Flash On",
                            Modifier,
                            MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

        )
    }
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


                activity.isCameraAvailable.value = true
            }
        }, modifier = modifier
    )
}