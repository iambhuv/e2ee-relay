package com.promtuz.chat.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class AppNavigator(val backStack: MutableList<NavKey>) {
    fun push(key: NavKey) {
        if (backStack.size > 1 && backStack[backStack.size - 2] == key) {
            backStack.removeLastOrNull()
        } else if (backStack.last() != key) backStack.add(key)
    }

    fun back(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
            return true
        }
        return false
    }

    fun chat(key: Route.ChatScreen) {
        if (backStack.size >= 2 && backStack[backStack.size - 2] == key) {
            backStack.removeLastOrNull()
        } else if (backStack.last() != key) {
            if (backStack.size >= 2 && backStack[backStack.size - 2] == Route.App) backStack.removeLastOrNull()
            backStack.add(key)
        }
    }
}
