package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.data.remote.ConnectionError
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.koinInject

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    hazeState: HazeState, innerPadding: PaddingValues, quicClient: QuicClient = koinInject()
) {
    val activity = LocalActivity.current as AppCompatActivity

    quicClient.connect(LocalContext.current, object : QuicClient.Listener {
        override fun onConnectionFailure(e: ConnectionError) {

        }

        override fun onConnectionSuccess() {

        }
    })

    Box {
        Column(Modifier.hazeSource(hazeState)) {
            StatsBox(innerPadding)

            val qrScanner = remember {
                QrScanner({ bytes ->

                }, { e ->

                })
            }

            Button({
                qrScanner.show(activity.supportFragmentManager, "QR Scanner")
            }) {
                Text("Scan QR")
            }
        }
    }
}

fun formatHex(bytes: ByteArray?): String {
    if (bytes == null) return "nil"
    return bytes.asSequence().map { "%02X".format(it) }.chunked(16) { it.joinToString(" ") }
        .joinToString("\n")
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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