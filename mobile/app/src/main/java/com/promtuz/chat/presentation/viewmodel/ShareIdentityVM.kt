package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.utils.media.ImageUtils
import dev.shreyaspatil.capturable.controller.CaptureController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShareIdentityVM(
    private val application: Application,
    private val userRepository: UserRepository,
    private val imgUtils: ImageUtils
) : ViewModel() {
    private val context: Context get() = application.applicationContext
    private lateinit var user: User

    private var _publicIdentity = MutableStateFlow<Identity?>(null)
    val publicIdentity = _publicIdentity.asStateFlow()

    init {
        viewModelScope.launch {
            user = userRepository.getCurrentUser()
            _publicIdentity.value = Identity(user.key, user.nickname)
        }
    }


    fun shareQrCode(captureController: CaptureController, shareCallback: (shareIntent: Intent) -> Unit) {
        viewModelScope.launch {
            val bitmapAsync = captureController.captureAsync()
            try {
                val bitmap = bitmapAsync.await()
                val uri = imgUtils.saveImageCache(bitmap.asAndroidBitmap())

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setType("image/png")
                }

                shareCallback(Intent.createChooser(shareIntent, "Share QR Code"))
            } catch (error: Exception) {
                Toast.makeText(context, "Failed to generate QR Image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}