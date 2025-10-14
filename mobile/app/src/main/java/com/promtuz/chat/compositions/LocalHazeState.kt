package com.promtuz.chat.compositions

import androidx.compose.runtime.compositionLocalOf

import dev.chrisbanes.haze.HazeState

val LocalHazeState = compositionLocalOf { HazeState() }