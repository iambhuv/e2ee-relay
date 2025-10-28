package com.promtuz.chat.ui.activities

import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.BuildCompat
import com.promtuz.chat.navigation.AppNavigation
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class App : AppCompatActivity() {
    private val appViewModel: AppViewModel by inject<AppViewModel>()
    private val keyManager: KeyManager by inject<KeyManager>()

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
        ExperimentalStdlibApi::class
    )
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
                AppNavigation()
            }
        }
    }
}