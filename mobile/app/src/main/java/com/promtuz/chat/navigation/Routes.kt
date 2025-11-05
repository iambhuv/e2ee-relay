package com.promtuz.chat.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object Routes : NavKey {
    @Serializable
    data object App : NavKey

    @Serializable
    data class Chat(val userId: String) : NavKey

    @Serializable
    data object Setting : NavKey

    @Serializable
    data object SavedUsers : NavKey
}