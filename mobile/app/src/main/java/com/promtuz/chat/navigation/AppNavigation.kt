package com.promtuz.chat.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.presentation.viewmodel.AppViewModel
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.screens.HomeScreen
import com.promtuz.chat.ui.screens.ShareIdentityScreen
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
    appViewModel: AppViewModel = koinViewModel()
) {
    val activity = LocalActivity.current
    val navigator = appViewModel.navigator
    val backStack = appViewModel.backStack

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
            entry<Route.ShareIdentityScreen> { ShareIdentityScreen() }
        },
        sizeTransform = SizeTransform(clip = false),
        transitionSpec = {
            ContentTransform(
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(550, easing = FastOutSlowInEasing),
                ), slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(550, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(550), targetAlpha = 0.75f
                ),
                targetContentZIndex = 1f
            )
        },
        popTransitionSpec = {
            ContentTransform(
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(550, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(550), initialAlpha = 0.75f
                ), slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(550, easing = FastOutSlowInEasing)
                ),
                targetContentZIndex = 0f
            )
        })
}