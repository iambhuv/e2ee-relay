package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.presentation.state.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QrScannerVM(
    private val application: Application
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val _isCameraAvailable = MutableStateFlow(false)
    val isCameraAvailable = _isCameraAvailable.asStateFlow()

    private val _cameraPermissionState = MutableStateFlow(PermissionState.NotRequested)
    val cameraPermissionState = _cameraPermissionState.asStateFlow()

    private val _cameraProviderState = MutableStateFlow<ProcessCameraProvider?>(null)
    val cameraProviderState = _cameraProviderState.asStateFlow()

    private val _identities = MutableStateFlow<List<Identity>>(emptyList())
    val identities = _identities.asStateFlow()

    fun refreshIdentities(local: Set<Identity>) {
        _identities.value = if (local.isEmpty()) emptyList()
        else (_identities.value + local).associateBy { it.key }.values.toList()
    }


    fun setCameraProvider(provider: ProcessCameraProvider) {
        _cameraProviderState.value = provider
    }

    fun handleCameraPermissionRequest(isGranted: Boolean) {
        if (isGranted) {
            _cameraPermissionState.value = PermissionState.Granted
        } else {
            _cameraPermissionState.value = PermissionState.Denied
        }
    }

    fun makeCameraAvailable() {
        _isCameraAvailable.value = true
    }
}