package com.promtuz.chat.ui.screens

import android.content.ClipData
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import androidx.navigation3.runtime.rememberNavBackStack
import com.promtuz.chat.R
import com.promtuz.chat.compositions.LocalNavigator
import com.promtuz.chat.navigation.AppRoutes
import com.promtuz.chat.navigation.Navigator
import com.promtuz.chat.ui.activities.QrScanner
import com.promtuz.chat.ui.components.BackTopBar
import com.promtuz.chat.ui.components.QrCode
import com.promtuz.chat.ui.text.avgSizeInStyle
import com.promtuz.chat.ui.theme.PromtuzTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareIdentityScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val activity = LocalActivity.current as AppCompatActivity
    val theme = MaterialTheme
    val scope = rememberCoroutineScope()

    val qrScanner = remember {
        QrScanner({ bytes ->
            println("QR SCAN RESULT : ${bytes.toHexString()}")
        }, { e ->

        })
    }


    Scaffold(
        topBar = {
            BackTopBar {
//                Text("Share Identity Key", style = MaterialTheme.typography.titleMediumEmphasized)
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
                val bytes = byteArrayOf(
                    12,
                    3,
                    2,
                    31,

                    123,
                    123,
                    123,
                    12,

                    3,
                    3,
                    123,
                    23,

                    3,
                    2,
                    31,
                    123,

                    123,
                    123,
                    12,
                    3,

                    3,
                    123,
                    23,
                    3,

                    31,
                    123,
                    123,
                    12,

                    3,
                    3,
                    123,
                    23,
                )

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
                        qrScanner.show(activity.supportFragmentManager, "QR Code Scanner")
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


@Preview
@Composable
fun QrScreenPreview() {
    PromtuzTheme(darkTheme = true) {
        val backStack = rememberNavBackStack(
            AppRoutes.App
        )
        val navigator = Navigator(backStack)

        CompositionLocalProvider(LocalNavigator provides navigator) {
            ShareIdentityScreen()
        }
    }
}
