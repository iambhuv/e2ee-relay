package com.promtuz.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.koin.compose.koinInject

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    hazeState: HazeState, innerPadding: PaddingValues, quicClient: QuicClient = koinInject()
) {
    Box {
        Column(Modifier.hazeSource(hazeState)) {
            // val listState = rememberLazyListState()

            StatsBox(innerPadding)

//            val chats = remember {
//                arrayOf(
//                    "Averal Purwar",
//                    "Aftab Shaikh",
//                    "Criminal",
//                    "Shaurya Ranjan",
//                    "Kabir",
//                    "Dynoxy",
//                )
//            }
//
//
//            LazyColumn(
//                state = listState,
//                contentPadding = PaddingValues(
//                    top = innerPadding.calculateTopPadding(),
//                    bottom = innerPadding.calculateBottomPadding()
//                ),
//                modifier = Modifier
//                    .fillMaxSize()
//            ) {
//                items(chats.size) { index ->
//                    val peer = chats[index];
//                    HomeListItem(peer)
//                }
//            }
        }
    }
}

fun formatHex(bytes: ByteArray?): String {
    if (bytes == null) return "nil"
    return bytes.asSequence()
        .map { "%02X".format(it) }
        .chunked(16) { it.joinToString(" ") }
        .joinToString("\n")
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsBox(
    innerPadding: PaddingValues,
    keyManager: KeyManager = koinInject(),
    quicClient: QuicClient = koinInject()
) {
    val keys = mapOf(
        "IDENTITY SECRET KEY" to keyManager.getSecretKey(),
        "IDENTITY PUBLIC KEY" to keyManager.getPublicKey(),
        "SERVER PUBLIC KEY" to quicClient.handshake.serverPublicKey?.bytes
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
        for ((text, bytes) in keys) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                modifier = Modifier.padding(
                    bottom = 4.dp,
                    top = if (text == keys.keys.first()) 0.dp else 8.dp
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
