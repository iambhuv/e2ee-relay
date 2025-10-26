package com.promtuz.chat.compositions

import androidx.compose.runtime.compositionLocalOf
import com.promtuz.chat.navigation.Navigator

val LocalNavigator = compositionLocalOf<Navigator> { error("Navigator not provided") }