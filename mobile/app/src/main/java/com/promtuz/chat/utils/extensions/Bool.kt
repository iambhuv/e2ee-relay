package com.promtuz.chat.utils.extensions

inline fun Boolean.then(block: () -> Unit) {
    if (this) block()
}