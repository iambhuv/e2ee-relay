package com.promtuz.chat.data.repository

import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.security.KeyManager

class UserRepository(
    private val users: UserDao,
    private val keyManager: KeyManager
) {
    suspend fun getCurrentUser(): User {
        val byte = keyManager.getPublicKey()
        return users.get(byte.asList())
    }
}