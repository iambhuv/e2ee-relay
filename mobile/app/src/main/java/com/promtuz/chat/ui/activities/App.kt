package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import com.promtuz.chat.navigation.AppNavigation
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import org.koin.android.ext.android.inject

class App : AppCompatActivity() {
    private lateinit var keyManager: KeyManager

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
        ExperimentalStdlibApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        keyManager = inject<KeyManager>().value
        keyManager.initialize()

        enableEdgeToEdge()

        setContent {
            PromtuzTheme {
                AppNavigation()
            }
        }
    }
}