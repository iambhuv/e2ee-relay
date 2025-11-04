package com.promtuz.chat.domain.model

import com.promtuz.chat.data.local.entities.User

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