package com.promtuz.chat.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class Navigator(val backStack: NavBackStack<NavKey>) {
    fun push(key: NavKey) {
        if (backStack.size > 1 && backStack[backStack.size - 2] == key) {
            backStack.removeLastOrNull()
        } else if (backStack.last() != key) backStack.add(key)
    }

    fun back() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun chat(key: AppRoutes.ChatScreen) {
        if (backStack.size >= 2 && backStack[backStack.size - 2] == key) {
            backStack.removeLastOrNull()
        } else if (backStack.last() != key) {
            if (backStack.size >= 2 && backStack[backStack.size - 2] == AppRoutes.App) backStack.removeLastOrNull()
            backStack.add(key)
        }
    }
}
