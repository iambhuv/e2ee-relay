package com.promtuz.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.ui.theme.adjustLight

@Composable
fun EncryptionKeyScreen(modifier: Modifier = Modifier) {
    var encryptionKey by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),

            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TODO: Move Constants to Separate Files
            Text(
                "Encryption Key",
                modifier = Modifier.padding(bottom = 6.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(48.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = encryptionKey,
                onValueChange = {
                    encryptionKey = it
                },
                label = {
                    Text(
                        "***********", color = adjustLight(
                            MaterialTheme.colorScheme.background, 0.6f
                        ),
                        fontWeight = FontWeight.W500
                    )
                },
                colors = outlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    false,
                    onCheckedChange = { },
                )

                Text("Remember me?", color = MaterialTheme.colorScheme.onBackground)
            }

            Button(
                {

                }, Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Continue", fontWeight = FontWeight.W500, fontSize = 16.sp)
            }
        }

        End2EndEncrypted(Modifier.align(BiasAlignment(0f, 0.85f)))
    }
}