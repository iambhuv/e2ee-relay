package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Route
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class AppVM(
    private val application: Application
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    val closeDrawer = MutableSharedFlow<Unit>()

    var backStack = NavBackStack<NavKey>(Route.App)
    val navigator = AppNavigator(backStack)

    /**
     * This apparently closes the HomeScreenDrawer if open, whereas [AppNavigator.push] doesn't
     */
    fun goTo(key: NavKey) {
        viewModelScope.launch {
            closeDrawer.emit(Unit)
            navigator.push(key)
        }
    }
}