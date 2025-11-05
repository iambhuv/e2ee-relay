package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.*
import com.promtuz.chat.navigation.AppNavigation
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class App : AppCompatActivity() {
    private val viewModel by viewModel<AppVM>()
    private val keyManager: KeyManager by inject<KeyManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyManager.initialize()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb(),
            ),
        )

        setContent {
            PromtuzTheme {
                AppNavigation(viewModel)
            }
        }
    }
}