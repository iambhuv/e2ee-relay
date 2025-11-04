package com.promtuz.chat.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.window.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.presentation.viewmodel.AppVM
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.screens.HomeScreen
import com.promtuz.chat.ui.screens.SavedUsersScreen
import kotlinx.serialization.Serializable


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
    data object SavedUsersScreen : NavKey

    @Serializable
    data object ShareIdentityScreen : NavKey
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigation(
    appViewModel: AppVM
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
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.App> { HomeScreen(appViewModel) }
            entry<Route.ProfileScreen> { Text("Profile") }
            entry<Route.ChatScreen> { key -> ChatScreen(key.userId) }
            entry<Route.SavedUsersScreen>(
                metadata = DialogSceneStrategy.dialog(
                    DialogProperties(windowTitle = "Saved Users")
                )
            ) { SavedUsersScreen() }
        },
        sizeTransform = SizeTransform(clip = false),
        transitionSpec = {
            fadeIn(tween(300, easing = LinearOutSlowInEasing)) +
                    slideInHorizontally { it / 5 } togetherWith
                    fadeOut(tween(200, easing = FastOutLinearInEasing)) +
                    slideOutHorizontally { -it / 10 }
        },
        popTransitionSpec = {
            fadeIn(tween(250, easing = LinearOutSlowInEasing)) +
                    slideInHorizontally { -it / 8 } togetherWith
                    fadeOut(tween(400, easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f))) +
                    slideOutHorizontally { it / 5 }
        }

    )
}