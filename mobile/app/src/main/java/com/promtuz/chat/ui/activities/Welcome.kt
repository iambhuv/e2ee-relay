package com.promtuz.chat.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.WelcomeScreen
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.android.ext.android.inject


/**
 * TODO:
 *  - Current WelcomeScreen is very bland and unattractive, so gotta fix that as well,
 *  - Gotta improve the UX as well.
 *  - When generating the key pair, must ask user for proper way of storing / backing-up
 *    the secret key via a dialog box *i suppose*
 *  - Password must be hashed using very high computation power demanding Argon2id config,
 *    doing such prevents brute-forcing the decryption of private key at huge extent
 *  - The exact config of hashing will not be standardized for app,
 *    but rather be chosen via circumstances of course backed by limits for minimum
 *  - Must add support for automatically storing the backup on certain cloud storage services like Google Drive
 *
 *  User must provide a "secure" password for encrypting the secret key.
 *  Hence, this encrypted blob is somewhat safe to move around *i suppose?*.
 */
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