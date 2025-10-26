package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.compositions.LocalNavigator
import com.promtuz.chat.data.remote.ConnectionError
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.navigation.AppRoutes
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    quicClient: QuicClient = koinInject(),
    keyManager: KeyManager = koinInject()
) {
    val navigator = LocalNavigator.current

    quicClient.connect(LocalContext.current, object : QuicClient.Listener {
        override fun onConnectionFailure(e: ConnectionError) {

        }

        override fun onConnectionSuccess() {

        }
    })

    Box {
        Column {
            StatsBox(innerPadding)

//            keyManager.getPublicKey()?.let {
//                QrCode(
//                    it, Modifier
//                        .fillMaxWidth(0.6f)
//                        .aspectRatio(1f)
//                        .align(Alignment.CenterHorizontally)
//                )
//            }

//            val qrScanner = remember {
//                QrScanner({ bytes ->
//                    println("QR SCAN RESULT : ${bytes.toHexString()}")
//                }, { e ->
//
//                })
//            }

            Button({
                navigator.push(AppRoutes.QrScreen)
                // qrScanner.show(activity.supportFragmentManager, "QR Scanner")
            }) {
                Text("Scan QR")
            }
        }
    }
}

fun formatHex(bytes: ByteArray?, c: Int = 16): String {
    if (bytes == null) return "nil"
    return bytes.asSequence().map { "%02X".format(it) }.chunked(c) { it.joinToString(" ") }
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