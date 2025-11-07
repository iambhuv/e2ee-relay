package com.promtuz.chat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.promtuz.chat.data.dummy.dummyChats
import com.promtuz.chat.data.repository.UserRepository
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.presentation.viewmodel.ChatVM
import com.promtuz.chat.ui.components.ChatBottomBar
import com.promtuz.chat.ui.components.ChatTopBar
import com.promtuz.chat.ui.components.MessageBubble
import com.promtuz.chat.ui.theme.PromtuzTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    appViewModel: AppVM,
    chatViewModel: ChatVM = koinViewModel(),
    userRepository: UserRepository = koinInject()
) {
    val direction = LocalLayoutDirection.current
    val chat = appViewModel.activeChatUser
    val colors = MaterialTheme.colorScheme
    val hazeState = rememberHazeState()
    val lazyState = rememberLazyListState(
        prefetchStrategy = LazyListPrefetchStrategy(nestedPrefetchItemCount = 8)
    )
    val interactionSource = remember { MutableInteractionSource() }

    if (chat != null) {
        Scaffold(
            Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = { ChatTopBar(chat, hazeState) },
            bottomBar = { ChatBottomBar(hazeState, interactionSource) }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
                    .background(colors.surface)
                    .padding(
                        start = padding.calculateLeftPadding(direction),
                        end = padding.calculateRightPadding(direction),
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 6.dp,
                    bottom = padding.calculateBottomPadding() + 6.dp,
                ),
                state = lazyState,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom)
            ) {
                item { MessageBubble("Maintenance tomorrow btw 9-11 AM, Cya later!") }
                item { MessageBubble("Haha") }
                item { MessageBubble("Sure") }
                item { MessageBubble("Sup") }
                item { MessageBubble("Hello") }
            }
        }
    } else appViewModel.navigator.back()
}