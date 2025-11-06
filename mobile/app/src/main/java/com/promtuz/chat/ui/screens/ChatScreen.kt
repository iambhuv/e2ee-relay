package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.dummy.dummyChats
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.presentation.viewmodel.ChatVM
import com.promtuz.chat.ui.components.Avatar
import com.promtuz.chat.ui.components.DrawableIcon
import com.promtuz.chat.ui.theme.PromtuzTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    appViewModel: AppVM,
    chatViewModel: ChatVM = koinViewModel(),
    userRepository: UserRepository = koinInject()
) {
    val chat = appViewModel.activeChatUser
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current

    if (chat != null) {
        Scaffold(
            Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = { ChatTopBar(chat) },
            bottomBar = { ChatBottomBar() }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(colors.surface)
                    .padding(padding)
            ) {
                item { Text("Chat Screen") }
            }
        }
    } else appViewModel.navigator.back()
}


@Composable
private fun ChatTopBar(chat: Chat) {
    val backHandler = LocalOnBackPressedDispatcherOwner.current
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography
    val backIndicationSource = remember { MutableInteractionSource() }

    val chatBarColors = TopAppBarDefaults.topAppBarColors(
        colors.surfaceContainerLow,
        subtitleContentColor = colors.onSurfaceVariant.copy(0.65f)
    )

    TopAppBar(
        title = {
            Text(chat.nickname, style = textStyle.titleMediumEmphasized.copy(fontSize = 18.sp))
        },
        subtitle = {
            Text("Last seen 2 min ago")
        },
        modifier = Modifier
            .fillMaxWidth(),
        navigationIcon = {
            Row(
                Modifier.padding(6.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DrawableIcon(
                    R.drawable.i_back_chevron, Modifier.clickable(
                        interactionSource = backIndicationSource,
                        indication = null // no ripple
                    ) {
                        backHandler?.onBackPressedDispatcher?.onBackPressed()
                    })
                Avatar(chat.nickname, 42.dp)
            }
        },
        actions = {
            IconButton({}) {
                DrawableIcon(R.drawable.i_ellipsis_vertical)
            }
        },
        colors = chatBarColors
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatBottomBar() {
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography
    val windowInfo = LocalWindowInfo.current

    var message by remember { mutableStateOf("") }
    val insetsPadding = ScaffoldDefaults.contentWindowInsets.asPaddingValues()

    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(bottom = insetsPadding.calculateBottomPadding())
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainerHigh)
            .heightIn(max = 0.25f * windowInfo.containerDpSize.height)
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onSurface) {
            BasicTextField(
                value = message,
                onValueChange = { message = it },
                cursorBrush = SolidColor(colors.onSurfaceVariant),
                textStyle = textStyle.bodyLargeEmphasized.copy(colors.onSurface),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) { innerTextField ->
                Box(Modifier.padding(12.dp, 8.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxWidth()) {
                        if (message.isEmpty()) Text(
                            "Message",
                            style = textStyle.bodyLargeEmphasized.copy(
                                colors.onSurfaceVariant.copy(0.8f)
                            )
                        )
                        innerTextField()
                    }
                }
            }
        }

        FilledIconButton(
            onClick = {},
            Modifier.align(Alignment.Bottom),
            colors = IconButtonDefaults.filledIconButtonColors(colors.primary),
            shape = RoundedCornerShape(18.dp)
        ) {
            DrawableIcon(R.drawable.i_send, Modifier.size(20.dp))
        }
    }

}


@Preview
@Composable
private fun ChatTopBarPreview() {
    val chat = dummyChats.random()

    PromtuzTheme(true) {
        ChatTopBar(chat)
    }
}


@Preview
@Composable
private fun ChatBottomBarPreview() {
    PromtuzTheme(true) {
        Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
            ChatBottomBar()
        }
    }
}