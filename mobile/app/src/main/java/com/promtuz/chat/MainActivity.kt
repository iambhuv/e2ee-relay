package com.promtuz.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.promtuz.chat.di.appModule
import com.promtuz.chat.di.authModule
import com.promtuz.chat.navigation.AppNavigation
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    private lateinit var keyManager: KeyManager

    @OptIn(
        ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
        ExperimentalStdlibApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        startKoin {
            androidContext(this@MainActivity)
            modules(
                appModule,
                authModule
            )
        }

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