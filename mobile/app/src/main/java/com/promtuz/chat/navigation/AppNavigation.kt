package com.promtuz.chat.navigation

// import androidx.navigation3.runtime.remember
// import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
// import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.promtuz.chat.compositions.LocalBackStack
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.ui.screens.ChatScreen
import com.promtuz.chat.ui.screens.EncryptionKeyScreen
import com.promtuz.chat.ui.screens.WelcomeScreen
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
sealed interface AppNavKey : NavKey

object AppRoutes {
    @Serializable
    data object App : AppNavKey

    @Serializable
    data class ChatScreen(val userId: String) : AppNavKey

    @Serializable
    data class ProfileScreen(val userId: String) : AppNavKey

    @Serializable
    data object SettingScreen : AppNavKey


    // === AUTHENTICATION SCREENS START ===

    @Serializable
    data object WelcomeScreen : AppNavKey

    @Serializable
    data object EncryptionKeyScreen : AppNavKey

    // === AUTHENTICATION SCREENS END ===
}


@Composable
fun AppNavigation(keyManager: KeyManager = koinInject()) {
    val backStack = rememberNavBackStack(
        if (keyManager.hasSecretKey()) AppRoutes.App else AppRoutes.WelcomeScreen
    )

    CompositionLocalProvider(LocalBackStack provides backStack) {
        NavDisplay(
            backStack,
            Modifier.background(MaterialTheme.colorScheme.background),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
//                    rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
//                    rememberSceneSetupNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is AppRoutes.App -> {
                        NavEntry(key, content = { HomeNavigation() })
                    }

                    is AppRoutes.ProfileScreen -> {
                        NavEntry(key, content = { Text("Sup Homie") })
                    }

                    is AppRoutes.ChatScreen -> {
                        NavEntry(key, content = { ChatScreen(key.userId) })
                    }

                    is AppRoutes.WelcomeScreen -> {
                        NavEntry(key, content = { WelcomeScreen() })
                    }

                    is AppRoutes.EncryptionKeyScreen -> {
                        NavEntry(key, content = { EncryptionKeyScreen() })
                    }

                    else -> throw RuntimeException("Invalid Screen")
                }
            },
            transitionSpec = {
                // Forward: new screen slides in from 30% while fading in
                // Previous screen fades out subtly and stays in place2
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