package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.promtuz.chat.navigation.AppNavigator
import com.promtuz.chat.navigation.Route

class AppViewModel(
    private val application: Application
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    var backStack = NavBackStack<NavKey>(Route.App)
    val navigator = AppNavigator(backStack)
}