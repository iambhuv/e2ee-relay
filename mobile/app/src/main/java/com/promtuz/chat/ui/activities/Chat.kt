package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.*
import com.promtuz.chat.data.dummy.dummyChats
import com.promtuz.chat.presentation.viewmodel.ChatVM
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class Chat : AppCompatActivity() {
    private val viewModel by viewModel<ChatVM>()
    private val keyManager: KeyManager by inject<KeyManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userIdentity = intent.getByteArrayExtra("user")?.takeIf { it.size == 32 } ?: return finish()
        // FIXME: very temporary
        val chat = dummyChats.find { it.identity.contentEquals(userIdentity) } ?: return finish()


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
                ChatScreen(
                    chat,
                    viewModel
                )
            }
        }
    }
}