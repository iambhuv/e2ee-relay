package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.promtuz.chat.presentation.viewmodel.ShareIdentityVM
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.ShareIdentityScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShareIdentity : AppCompatActivity() {
    private val viewModel: ShareIdentityVM by viewModel()
    private lateinit var keyManager: KeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyManager = inject<KeyManager>().value
        keyManager.initialize()

        enableEdgeToEdge()

        setContent {
            PromtuzTheme {
                ShareIdentityScreen(viewModel)
            }
        }
    }
}