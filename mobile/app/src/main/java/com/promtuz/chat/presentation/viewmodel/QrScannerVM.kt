package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.domain.model.UserIdentity
import com.promtuz.chat.presentation.state.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class QrScannerVM(
    private val application: Application,
    private val userRepository: UserRepository
) : ViewModel() {
    private val context: Context get() = application.applicationContext
    private val log = Timber.tag("QrScannerVM")

    var imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val _isCameraAvailable = MutableStateFlow(false)
    val isCameraAvailable = _isCameraAvailable.asStateFlow()

    private val _cameraPermissionState = MutableStateFlow(PermissionState.NotRequested)
    val cameraPermissionState = _cameraPermissionState.asStateFlow()

    private val _cameraProviderState = MutableStateFlow<ProcessCameraProvider?>(null)
    val cameraProviderState = _cameraProviderState.asStateFlow()

    private val _identities = MutableStateFlow<List<UserIdentity>>(emptyList())
    val identities = _identities.asStateFlow()

    private val _identitiesBeingSaved = MutableStateFlow<List<UserIdentity>>(emptyList())
    val identitiesBeingSaved = _identitiesBeingSaved.asStateFlow()

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

    fun handleScannedBarcodes(barcodes: List<Barcode>) = viewModelScope.launch {
        _identities.value = (barcodes.mapNotNull { barcode ->
            val identity =
                barcode.rawBytes?.let {
                    log.d("IDENTITY: ${it.toHexString()}")
                    Identity.fromByteArray(it)
                } ?: return@mapNotNull null

            val user = userRepository.fromIdentity(identity)
            UserIdentity(user, identity)
        }).distinctBy { it }
    }

    fun saveUserIdentity(userIdentity: UserIdentity) {
        _identitiesBeingSaved.update { it + userIdentity }

        viewModelScope.launch {
            try {
                userRepository.save(userIdentity.user)
            } finally {
                _identitiesBeingSaved.update { it - userIdentity }
            }
        }
    }
}