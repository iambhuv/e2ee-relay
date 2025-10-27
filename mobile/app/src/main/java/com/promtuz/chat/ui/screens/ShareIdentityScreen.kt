package com.promtuz.chat.ui.screens

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
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
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.components.QrCode
import com.promtuz.chat.ui.text.avgSizeInStyle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareIdentityScreen(
    keyManager: KeyManager = koinInject()
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    // val activity = LocalActivity.current as AppCompatActivity
    val theme = MaterialTheme
    val scope = rememberCoroutineScope()

    val qrLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContract<Unit, ByteArray?>() {
            override fun createIntent(context: Context, input: Unit) =
                Intent(context, QrScanner::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?) =
                intent?.getByteArrayExtra("qr_result")
        }
    ) { bytes ->
        println("QR SCAN RESULT: ${bytes?.toHexString()}")
    }

    val bytes = remember { keyManager.getPublicKey() }
        ?: throw Exception("Identity Public Key Unavailable in Share Screen")

    Scaffold(
        topBar = {
            BackTopBar {
                Text(
                    "Share Identity Key", style = avgSizeInStyle(
                        theme.typography.titleLargeEmphasized,
                        theme.typography.titleMediumEmphasized
                    )
                )
            }
        }) { innerPadding ->
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
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                QrCode(
                    bytes, Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .aspectRatio(1f)
                )

                Text(
                    bytes.toHexString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                "Public Identity Key",
                                                bytes.toHexString()
                                            )
                                        )
                                    )

                                    Toast.makeText(
                                        context,
                                        "Public Identity Key copied to clipboard",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        ),
                    textAlign = TextAlign.Center,
                    style = theme.typography.bodyLargeEmphasized,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }


            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    {},
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally),

                    ) {
                    Text(
                        "Share QR Code",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
                    )
                }

                TextButton(
                    {
                        qrLauncher.launch(Unit)
                        // qrScanner.show(activity.supportFragmentManager, "QR Code Scanner")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally),

                    ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.i_qr_code_scanner),
                            "QR Code Scanner Icon"
                        )

                        Text(
                            "Scan QR Code",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLargeEmphasized.copy(fontSize = MaterialTheme.typography.labelLargeEmphasized.fontSize),
                        )
                    }
                }
            }
        }
    }
}