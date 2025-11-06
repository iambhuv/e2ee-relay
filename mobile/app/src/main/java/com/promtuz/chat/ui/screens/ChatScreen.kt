package com.promtuz.chat.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.R
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.domain.model.Chat
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.presentation.viewmodel.ChatVM
import com.promtuz.chat.ui.components.Avatar
import com.promtuz.chat.ui.components.DrawableIcon
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    appViewModel: AppVM,
    chatViewModel: ChatVM = koinViewModel(),
    userRepository: UserRepository = koinInject()
) {
    val chat = appViewModel.activeChatUser
    val colors = MaterialTheme.colorScheme

    if (chat != null) {
        Scaffold(
            Modifier.fillMaxSize(),
            topBar = { ChatTopBar(chat) },
            bottomBar = { ChatBottomBar() }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.surface)
                    .padding(padding)
            ) {
                Text("Chat Screen")
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

@Composable
private fun ChatBottomBar() {

}


