@file:androidx.annotation.OptIn(ExperimentalGetImage::class) @file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class
)

package com.promtuz.chat.ui.screens

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.activities.ShareIdentity
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.components.QrCode
import com.promtuz.chat.ui.text.avgSizeInStyle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun ShareIdentityScreen(
    activity: ShareIdentity,
    keyManager: KeyManager = koinInject()
) {
    val theme = MaterialTheme

    val key = remember { keyManager.getPublicKey() }
        ?: throw Exception("Identity Public Key Unavailable in Share Screen")

    val identity = Identity(key.asList())

    Scaffold(
        topBar = { TopBar() }) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically)
        ) {

            Column(
                Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(32.dp))
                    .background(theme.colorScheme.surfaceContainer)
                    .padding(32.dp), verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                QrCode(
                    identity.toByteArray(),
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .aspectRatio(1f)
                )

                // PublicKeyBytesHex(bytes)
            }

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShareQRButton()
                ScanQRButton()
            }
        }
    }
}


@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    val textTheme = MaterialTheme.typography

    BackTopBar("Share Identity Key")
}


@Composable
private fun PublicKeyBytesHex(bytes: ByteArray, modifier: Modifier = Modifier) {
    val textTheme = MaterialTheme.typography
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Text(
        bytes.toHexString(),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {
                scope.launch {
                    val clipData = ClipData.newPlainText("Public Identity Key", bytes.toHexString())
                    val clipEntry = ClipEntry(clipData)
                    clipboard.setClipEntry(clipEntry)

                    Toast.makeText(
                        context, "Public Identity Key copied to clipboard", Toast.LENGTH_LONG
                    ).show()
                }
            }),
        textAlign = TextAlign.Center,
        style = textTheme.bodyLargeEmphasized,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ColumnScope.ShareQRButton(modifier: Modifier = Modifier) {
    Button(
        {},
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Text(
            "Share QR Code",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
        )
    }
}


@Composable
private fun ColumnScope.ScanQRButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    TextButton(
        {
            context.startActivity(Intent(context, QrScanner::class.java))
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
            .align(Alignment.CenterHorizontally),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.i_qr_code_scanner), "QR Code Scanner Icon"
            )

            Text(
                "Scan QR Code",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
            )
        }
    }
}