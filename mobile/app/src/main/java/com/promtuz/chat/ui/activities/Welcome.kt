package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.WelcomeScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.android.ext.android.inject

class Welcome : ComponentActivity() {
    private lateinit var keyManager: KeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyManager = inject<KeyManager>().value
        keyManager.initialize()

        enableEdgeToEdge()

        setContent {
            PromtuzTheme {
                WelcomeScreen()
            }
        }
    }
}