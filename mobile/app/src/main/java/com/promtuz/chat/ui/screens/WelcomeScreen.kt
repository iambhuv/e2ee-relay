package com.promtuz.chat.ui.screens

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.di.authModule
import com.promtuz.chat.presentation.state.WelcomeField
import com.promtuz.chat.presentation.state.WelcomeStatus
import com.promtuz.chat.presentation.viewmodel.WelcomeViewModel
import com.promtuz.chat.ui.activities.App
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
    welcomeViewModel: WelcomeViewModel = koinViewModel()
) {

    val context = LocalContext.current

    val state by welcomeViewModel.uiState
    val isTryingToContinue by remember { derivedStateOf { state.status != WelcomeStatus.Normal } }

    val focusManager = LocalFocusManager.current

    Box(
        modifier
            .fillMaxSize()
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
                value = state.nickname,
                onValueChange = { welcomeViewModel.onChange(WelcomeField.Nickname, it) },
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
                    text ?: "", fontSize = 14.sp, color = outlinedFormElementsColors().error.label
                )

            }

            Button(
                {
                    welcomeViewModel.`continue` {
                        context.startActivity(Intent(context, App::class.java))
                        (context as? Activity)?.finish()
                    }
                },
                Modifier.fillMaxWidth(),
            ) {
                AnimatedContent(
                    state.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RectangleShape),
                    contentAlignment = Alignment.Center,
                    transitionSpec = { Buttonimations.labelSlide() }) { status ->
                    Text(
                        stringResource(status.text),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer { // Allow overflow
                            clip = false
                        })
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
                        context, "Importing is not supported yet.", Toast.LENGTH_SHORT
                    ).show()
                }, Modifier.fillMaxWidth(), border = BorderStroke(
                    width = 1.dp, color = outlinedFormElementsColors().unfocused.border
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