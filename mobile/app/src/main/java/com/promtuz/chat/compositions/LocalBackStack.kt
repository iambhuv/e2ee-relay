package com.promtuz.chat.compositions

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

val LocalBackStack = compositionLocalOf { NavBackStack<NavKey>() }