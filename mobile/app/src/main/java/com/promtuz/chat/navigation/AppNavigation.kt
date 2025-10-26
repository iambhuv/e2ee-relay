package com.promtuz.chat.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.compositions.LocalNavigator
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.screens.HomeScreen
import com.promtuz.chat.ui.screens.ShareIdentityScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject


object AppRoutes : NavKey {
    @Serializable
    data object App : NavKey

    @Serializable
    data class ChatScreen(val userId: String) : NavKey

    @Serializable
    data class ProfileScreen(val userId: String) : NavKey

    @Serializable
    data object SettingScreen : NavKey

    @Serializable
    data object QrScreen : NavKey
}


@Composable
fun AppNavigation(keyManager: KeyManager = koinInject()) {
    val backStack = rememberNavBackStack(
        AppRoutes.App
    )
    val navigator = Navigator(backStack)

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack,
            Modifier.background(MaterialTheme.colorScheme.background),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = { key ->
                when (key) {
                    is AppRoutes.App -> NavEntry(key) { HomeScreen() }
                    is AppRoutes.ProfileScreen -> NavEntry(key) { Text("Profile") }
                    is AppRoutes.ChatScreen -> NavEntry(key) { ChatScreen(key.userId) }
                    is AppRoutes.QrScreen -> NavEntry(key) { ShareIdentityScreen() }

                    else -> throw RuntimeException("Invalid Screen")
                }
            },
            transitionSpec = {
                (slideInHorizontally(
                    initialOffsetX = { (it * 0.3f).toInt() },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )) togetherWith fadeOut(
                    animationSpec = tween(250, easing = FastOutSlowInEasing), targetAlpha = 0.3f
                )
            },
            popTransitionSpec = {
                // Back: returning screen fades in from subtle state
                // Departing screen slides out to 30% while fading out
                fadeIn(
                    animationSpec = tween(250, easing = FastOutSlowInEasing), initialAlpha = 0.3f
                ) togetherWith (slideOutHorizontally(
                    targetOffsetX = { (it * 0.3f).toInt() },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ))
            },
            predictivePopTransitionSpec = {
                // Predictive back: smooth slide from left with consistent behavior
                (slideInHorizontally(
                    initialOffsetX = { -(it * 0.2f).toInt() },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(250, easing = FastOutSlowInEasing), initialAlpha = 0.3f
                )) togetherWith (slideOutHorizontally(
                    targetOffsetX = { (it * 0.3f).toInt() },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ))
            })
    }
}