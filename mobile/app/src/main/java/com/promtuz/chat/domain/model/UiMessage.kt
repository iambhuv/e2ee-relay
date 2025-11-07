package com.promtuz.chat.domain.model

enum class UiMessagePosition {
    Single, Start, Middle, End
}

/**
 * UI friendly Message model.
 * Basically plain text content and ui related information
 */
class UiMessage(
    val id: String,
    val content: String,
    val isSent: Boolean,

    // UI Decorations
    val position: UiMessagePosition,

    private val timestamp: Long
)