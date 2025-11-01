@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Rational
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.promtuz.chat.R
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.text.avgSizeInStyle
import com.promtuz.chat.ui.views.QrOverlayView


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

        LaunchedEffect(cameraPermission) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                activity.initScanner()
            }
        }

        cameraProvider?.let {
            CameraPreview(
                activity, it, Modifier
                    .fillMaxSize()
                    .onSizeChanged { (w, h) ->
                        activity.viewSize.value = Size(w.toFloat(), h.toFloat())
                    })
        }

        if (cameraPermission != PermissionState.Granted) {
            activity.requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            Column(Modifier.align(BiasAlignment(0f, 0.8f))) {
                Button({  }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.i_user_add), "Add Contact"
                        )
                        Text(buildAnnotatedString {
                            append("Add ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Titan")
                            }
                        })
                    }
                }
            }
        }

        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                )
            ),
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

                val previewOverlay = QrOverlayView(context).apply {
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