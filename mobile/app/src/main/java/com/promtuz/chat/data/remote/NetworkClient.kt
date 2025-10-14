package com.promtuz.chat.data.remote

import com.promtuz.chat.data.remote.api.ApiResult
import com.promtuz.chat.data.remote.dto.LoginRequest
import com.promtuz.chat.utils.compression.ZlibCompressor
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

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


    suspend fun login(username: String, password: String): ApiResult<String> {
        return safeApiCall {
            val rawPayload = Json.encodeToString(LoginRequest(username, password, false))
            val payload = ZlibCompressor.compress(rawPayload)
            httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.OctetStream)
                setBody(payload)
            }
        }
    }
}