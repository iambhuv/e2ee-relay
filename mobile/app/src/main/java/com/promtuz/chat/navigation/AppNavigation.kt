package com.promtuz.chat.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.screens.HomeScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel


object Route : NavKey {
    @Serializable
    data object App : NavKey

    @Serializable
    data class ChatScreen(val userId: String) : NavKey

    @Serializable
    data class ProfileScreen(val userId: String) : NavKey

    @Serializable
    data object SettingScreen : NavKey

    @Serializable
    data object ShareIdentityScreen : NavKey
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigation(
    appViewModel: AppVM = koinViewModel()
) {
    val activity = LocalActivity.current
    val navigator = appViewModel.navigator
    val backStack = appViewModel.backStack
    val motionScheme = MaterialTheme.motionScheme

    BackHandler(true) {
        if (!navigator.back()) {
            activity?.finish()
        }
    }

    NavDisplay(
        backStack,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.App> { HomeScreen() }
            entry<Route.ProfileScreen> { Text("Profile") }
            entry<Route.ChatScreen> { key -> ChatScreen(key.userId) }
        },
        sizeTransform = SizeTransform(clip = false),
        transitionSpec = {
            ContentTransform(
                fadeIn(motionScheme.slowEffectsSpec()),
                fadeOut(motionScheme.slowEffectsSpec()),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                fadeIn(motionScheme.slowEffectsSpec()),
                scaleOut(
                    targetScale = 0.7f,
                ) + fadeOut(targetAlpha = 0f),
            )
        },
    )
}