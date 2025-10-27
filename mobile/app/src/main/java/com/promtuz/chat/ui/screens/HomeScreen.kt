package com.promtuz.chat.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.activities.ShareIdentity
import com.promtuz.chat.ui.components.QrCode
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun HomeScreen(
    keyManager: KeyManager = koinInject(), appViewModel: AppViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BoxWithConstraints {
                val maxWidth = maxWidth * 0.8f

                ModalDrawerSheet(
                    modifier = Modifier.widthIn(min = 200.dp, max = maxWidth)
                ) {
                    keyManager.getPublicKey()?.let {
                        Text("Scan Me Please", modifier = Modifier.padding(16.dp))
                        HorizontalDivider()
                        QrCode(it, Modifier.padding(32.dp))
                        HorizontalDivider()
                    }
                }
            }
        },
    ) {
        Scaffold(topBar = { TopBar() }, floatingActionButton = {
            FloatingActionButton({
                context.startActivity(Intent(context, ShareIdentity::class.java))
            }) {
                Icon(
                    painter = painterResource(R.drawable.i_qr_code_scanner),
                    "QR Code",
                    Modifier,
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }) { innerPadding ->
            Column {
                StatsBox(innerPadding)
            }
        }
    }
}

fun formatHex(bytes: ByteArray?, c: Int = 16): String {
    if (bytes == null) return "nil"
    return bytes.asSequence().map { "%02X".format(it) }.chunked(c) { it.joinToString(" ") }
        .joinToString("\n")
}

@Composable
fun StatsBox(
    innerPadding: PaddingValues,
    keyManager: KeyManager = koinInject(),
    quicClient: QuicClient = koinInject()
) {
    val status by quicClient.status

    val keys = mapOf(
        "IDENTITY SECRET KEY" to keyManager.getSecretKey(),
        "IDENTITY PUBLIC KEY" to keyManager.getPublicKey(),
        "SERVER PUBLIC KEY" to quicClient.handshake?.serverPublicKey?.bytes
    )

    Column(
        Modifier
            .padding(innerPadding)
            .fillMaxWidth()
            .padding(12.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)
    ) {
        Text("State : $status")

        for ((text, bytes) in keys) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                modifier = Modifier.padding(
                    bottom = 4.dp, top = if (text == keys.keys.first()) 0.dp else 8.dp
                )
            )
            Text(
                formatHex(bytes),
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
@Preview
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    PromtuzTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            StatsBox(PaddingValues.Zero)
        }
    }
}