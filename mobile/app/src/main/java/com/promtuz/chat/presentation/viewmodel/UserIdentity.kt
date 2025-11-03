package com.promtuz.chat.presentation.viewmodel

import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.domain.model.Identity

data class UserIdentity(
    val user: User,
    val identity: Identity
) {
    val key: String
        get() = user.key.toHexString()

    override fun equals(other: Any?) =
        other is UserIdentity && key == other.key

    override fun hashCode() = key.hashCode()
}