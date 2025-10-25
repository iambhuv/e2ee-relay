package com.promtuz.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.App
import com.promtuz.chat.ui.activities.Welcome
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LauncherActivity : ComponentActivity() {
    private lateinit var keyManager: KeyManager
    private lateinit var quicClient: QuicClient

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var keepSplashOnScreen = true

        splashScreen.setKeepOnScreenCondition {
            keepSplashOnScreen
        }

        keyManager = inject<KeyManager>().value
        keyManager.initialize()

        quicClient = inject<QuicClient>().value

        lifecycleScope.launch {
            try {
                if (keyManager.hasSecretKey()) {
                    startActivity(
                        Intent(this@LauncherActivity, App::class.java)
                    )
                } else {
                    startActivity(
                        Intent(this@LauncherActivity, Welcome::class.java)
                    )
                }

                finish()
            } finally {
                keepSplashOnScreen = false
            }
        }
    }
}