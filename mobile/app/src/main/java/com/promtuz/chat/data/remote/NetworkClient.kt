package com.promtuz.chat.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout

class NetworkClient {
    private val baseUrl = "http://localhost:8888"

    private val httpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }

        // For your zlib compression later
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
        }

        expectSuccess = false
    }

}