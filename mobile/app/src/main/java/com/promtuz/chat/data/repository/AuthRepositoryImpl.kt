package com.promtuz.chat.data.repository

import com.promtuz.chat.data.remote.NetworkClient
import com.promtuz.chat.data.remote.api.ApiResult

class AuthRepositoryImpl(
    private val networkClient: NetworkClient
) {
    suspend fun login(username: String, password: String): ApiResult<String> {
        return networkClient.login(username, password)
    }
}