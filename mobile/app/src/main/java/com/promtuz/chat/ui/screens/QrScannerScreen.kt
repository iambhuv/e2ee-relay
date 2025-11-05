@file:androidx.annotation.OptIn(ExperimentalGetImage::class)

package com.promtuz.chat.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.*
import androidx.core.view.doOnLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.promtuz.chat.R
import com.promtuz.chat.domain.model.UserIdentity
import com.promtuz.chat.presentation.state.PermissionState
import com.promtuz.chat.presentation.viewmodel.QrScannerVM
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.GoBackButton
import com.promtuz.chat.ui.text.avgSizeInStyle
import com.promtuz.chat.ui.views.QrOverlayView


@Composable
fun QrScannerScreen(
    activity: QrScanner,
    viewModel: QrScannerVM
) {
    val textTheme = MaterialTheme.typography

    var torchEnabled by remember { mutableStateOf(false) }
    val haveCamera by viewModel.isCameraAvailable.collectAsState()

    Box(
        Modifier.fillMaxSize()
    ) {
        val cameraPermission by viewModel.cameraPermissionState.collectAsState()
        val cameraProvider by viewModel.cameraProviderState.collectAsState()
        val identities by viewModel.identities.collectAsState()
        val identitiesBeingSaved by viewModel.identitiesBeingSaved.collectAsState()

        when (cameraPermission) {
            PermissionState.NotRequested -> {
                activity.requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            PermissionState.Denied -> {
                Column(
                    Modifier
                        .padding(32.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Camera permission denied. Enable it in Settings to scan QR",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Button({
                        activity.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                setData(Uri.fromParts("package", activity.packageName, null))
                            }
                        )
                    }) {
                        Text("Open Settings")
                    }
                }
            }

            PermissionState.Granted -> {
                activity.checkAndInitialize()
            }
        }

        cameraProvider?.let {
            CameraPreview(
                activity, it, Modifier
                    .fillMaxSize(), viewModel
            )
        }

        LazyColumn(
            Modifier.align(BiasAlignment(0f, 0.65f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(identities, { identity -> identity.key }) {
                IdentityActionButton(
                    it,
                    viewModel,
                    identitiesBeingSaved.contains(it),
                    Modifier.animateItem(
                        fadeInSpec = null
                    )
                )
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
            navigationIcon = { GoBackButton() }, title = {
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
                            MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

        )
    }
}

@Composable
private fun CameraPreview(
    activity: QrScanner,
    cameraProvider: ProcessCameraProvider,
    modifier: Modifier,
    viewModel: QrScannerVM
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
                    UseCaseGroup.Builder().addUseCase(preview).addUseCase(viewModel.imageAnalysis)
                        .setViewPort(viewPort).build()

                cameraProvider.unbindAll()
                activity.camera =
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)

                viewModel.makeCameraAvailable()
            }
        }, modifier = modifier
    )
}


@Composable
private fun IdentityActionButton(
    userIdentity: UserIdentity,
    vm: QrScannerVM,
    saving: Boolean,
    modifier: Modifier = Modifier
) {
    val user = userIdentity.user
    val identity = userIdentity.identity
    val isNew = user.isNew
    val name = identity.nickname.ifBlank { "Anonymous" }

    Button({
        vm.saveUserIdentity(userIdentity)
    }, modifier, enabled = isNew) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (saving) LoadingIndicator(Modifier.size(24.dp))
            else Icon(
                painter = if (isNew) painterResource(R.drawable.i_user_add) else painterResource(R.drawable.i_user_check),
                if (isNew) "Add Contact" else "Contact Saved"
            )

            Text(buildAnnotatedString {
                append("Add ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(name)
                }
            })
        }
    }
}