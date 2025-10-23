package com.promtuz.chat.navigation

import androidx.compose.animation.core.EaseInQuint
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.compositions.LocalHazeState
import com.promtuz.chat.ui.components.BlurredBars
import com.promtuz.chat.ui.components.BottomBar
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.screens.CallScreen
import com.promtuz.chat.ui.screens.FriendScreen
import com.promtuz.chat.ui.screens.HomeScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeNavKey : NavKey

object HomeRoutes {
    @Serializable
    data object HomeScreen : HomeNavKey

    @Serializable
    data object CallScreen : HomeNavKey

    @Serializable
    data object FriendScreen : HomeNavKey
}


data class ScrollState(val scroll: Int, val maxScroll: Int)

@Composable
fun HomeNavigation() {
    val backStack = rememberNavBackStack(HomeRoutes.HomeScreen)

    val localHazeState = rememberHazeState()
    val hazeState = rememberHazeState()
    val scrollState = remember { mutableStateOf(ScrollState(0, 0)) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(localHazeState),
        topBar = { TopBar() },
        bottomBar = { BottomBar(backStack) }
    ) { innerPadding ->
        CompositionLocalProvider(LocalHazeState provides localHazeState) {
            Box {
                NavDisplay(
                    backStack,
                    entryDecorators = listOf(
//                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
//                        rememberSceneSetupNavEntryDecorator()
                    ),
                    entryProvider = { key ->
                        when (key) {
                            is HomeRoutes.HomeScreen -> {
                                NavEntry(key = key) {
                                    HomeScreen(hazeState, innerPadding)
                                }
                            }

                            is HomeRoutes.CallScreen -> {
                                NavEntry(key = key) {
                                    CallScreen(hazeState, innerPadding)
                                }
                            }

                            is HomeRoutes.FriendScreen -> {
                                NavEntry(key = key) {
                                    FriendScreen(hazeState, innerPadding)
                                }
                            }

                            else -> throw RuntimeException("Invalid Screen")
                        }
                    },
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(0, 0, EaseInQuint)
                        ) togetherWith fadeOut(
                            animationSpec = tween(0, 0, EaseOutQuint)
                        )
                    },
                    popTransitionSpec = {
                        fadeIn(
                            animationSpec = tween(0, 0, EaseInQuint)
                        ) togetherWith fadeOut(
                            animationSpec = tween(0, 0, EaseOutQuint)
                        )
                    },
                    predictivePopTransitionSpec = {
                        fadeIn(
                            animationSpec = tween(0, 0, EaseInQuint)
                        ) togetherWith fadeOut(
                            animationSpec = tween(0, 0, EaseOutQuint)
                        )
                    }
                )
//                BlurredBars(
//                    hazeState,
//                    // scrollState,
//                    innerPadding.calculateTopPadding(),
//                    Alignment.TopCenter
//                )
//                BlurredBars(
//                    hazeState,
//                    // scrollState,
//                    innerPadding.calculateBottomPadding(),
//                    Alignment.BottomCenter
//                )
            }
        }
    }
}