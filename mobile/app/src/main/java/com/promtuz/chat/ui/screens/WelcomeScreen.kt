package com.promtuz.chat.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promtuz.chat.R
import com.promtuz.chat.compositions.LocalBackStack
import com.promtuz.chat.di.authModule
import com.promtuz.chat.presentation.state.WelcomeField
import com.promtuz.chat.presentation.state.WelcomeStatus
import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.ui.components.OutlinedFormElements
import com.promtuz.chat.ui.constants.Buttonimations
import com.promtuz.chat.ui.constants.Tweens
import com.promtuz.chat.ui.theme.PromtuzTheme
import com.promtuz.chat.ui.theme.adjustLight
import com.promtuz.chat.ui.theme.outlinedFormElementsColors
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplicationPreview

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    vm: WelcomeViewModel = koinViewModel()
) {

    val context = LocalContext.current

    val state by vm.uiState
    val isTryingToContinue by remember { derivedStateOf { state.status != WelcomeStatus.Normal } }

    val focusManager = LocalFocusManager.current

    val backStack = LocalBackStack.current

    Box(
        modifier
            .fillMaxSize()
            // .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 120.dp, bottom = 0.dp)
                .align(Alignment.BottomCenter),
        ) {
            Text(
                stringResource(R.string.welcome_screen_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(32.dp))

            Text(
                stringResource(R.string.welcome_screen_name_label),
                style = MaterialTheme.typography.bodyMedium,
                color = outlinedFormElementsColors().focused.label,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            OutlinedFormElements.TextField(
                value = state.displayName,
                onValueChange = { vm.onChange(WelcomeField.DisplayName, it) },
                placeholder = stringResource(R.string.welcome_screen_example_name),
                enabled = !isTryingToContinue,
                readOnly = isTryingToContinue,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }),
            )

            Spacer(Modifier.height(6.dp))

            Spacer(Modifier.height(6.dp))

            val scope = rememberCoroutineScope()

            AnimatedContent(
                state.errorText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                transitionSpec = {
                    (slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = Tweens.microInteraction()
                    ) + fadeIn(Tweens.microInteraction())) togetherWith (slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = Tweens.microInteraction()
                    ) + fadeOut(Tweens.microInteraction()))
                }) { text ->

                Text(
                    text ?: "",
                    fontSize = 14.sp,
                    color = outlinedFormElementsColors().error.label
                )

            }

            Button(
                {
                    vm.`continue`()
                },
                Modifier.fillMaxWidth(),
            ) {
                AnimatedContent(
                    state.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RectangleShape),
                    contentAlignment = Alignment.Center,
                    transitionSpec = { Buttonimations.labelSlide() }
                ) { status ->
                    Text(
                        stringResource(status.text),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer { // Allow overflow
                            clip = false
                        }
                    )
                }
            }


            Spacer(Modifier.height(48.dp))

            Text(
                stringResource(R.string.welcome_screen_continue_existing_label),
                style = MaterialTheme.typography.bodyMedium,
                color = outlinedFormElementsColors().focused.label,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            TextButton(
                {
                    Toast.makeText(
                        context,
                        "Importing is not supported yet.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                },
                Modifier.fillMaxWidth(),
                border = BorderStroke(
                    width = 1.dp,
                    color = outlinedFormElementsColors().unfocused.border
                )
            ) {
                Text(
                    stringResource(R.string.welcome_screen_continue_existing_button),
                    fontWeight = FontWeight.W500,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 82.dp)
            ) {
                End2EndEncrypted(
                    Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 42.dp)
                )
            }
        }


        // End2EndEncrypted(Modifier.align(BiasAlignment(0f, 0.85f)))

    }
//    }
}

@Composable
fun outlinedTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = adjustLight(
            MaterialTheme.colorScheme.background, 0.3f
        ), focusedBorderColor = adjustLight(
            MaterialTheme.colorScheme.background, 0.5f
        )
    )
}


@Composable
fun End2EndEncrypted(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.i_encrypted),
            "Encrypted",
            Modifier.size(16.dp),
            tint = adjustLight(
                MaterialTheme.colorScheme.background, 0.6f
            )
        )

        Text(
            stringResource(R.string.e2ee),
            fontSize = 12.sp,
            fontWeight = FontWeight.W500,
            color = adjustLight(
                MaterialTheme.colorScheme.background, 0.6f
            )
        )
    }
}

@Composable
@Preview
fun WelcomeScreenPreview() {
    KoinApplicationPreview(application = { modules(authModule) }) {
        PromtuzTheme(darkTheme = true) {
            Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                WelcomeScreen()
            }
        }
    }
}