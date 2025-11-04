package com.promtuz.chat.data.repository

import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.domain.model.Identity
import com.promtuz.chat.security.KeyManager
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val users: UserDao,
    private val keyManager: KeyManager
) {

    /**
     * Fetches Current User based on stored public key
     *
     * @throws IllegalStateException if it doesn't exist, because it should exist
     */
    suspend fun getCurrentUser(): User {
        val byte = keyManager.getPublicKey()
        return users.get(byte)
            ?: throw IllegalStateException("current user must exist but doesn’t")
    }

    suspend fun fromIdentity(identity: Identity): User {
        return users.get(identity.key) ?: User(identity.key, identity.nickname).apply { isNew = true }
    }

    suspend fun save(user: User): User {
        if (!user.isNew) throw IllegalStateException("Attempted to save a non-new user")
        users.insert(user)
        return users.get(user.key) ?: throw IllegalStateException("data must exist but doesn’t")
    }

    fun fetchAll(nickname: String = ""): Flow<List<User>> {
        return if (nickname.isEmpty()) users.getAll() else users.getAll(nickname)
    }
}