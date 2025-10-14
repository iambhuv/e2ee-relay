package com.promtuz.chat.data.remote

import com.promtuz.chat.data.remote.api.ApiResult
import com.promtuz.chat.data.remote.dto.ApiResponse
import com.promtuz.chat.data.remote.dto.AppError
import com.promtuz.chat.data.remote.dto.ErrorType
import com.promtuz.chat.utils.compression.ZlibCompressor
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

suspend inline fun <reified T> safeApiCall(
    crossinline apiCall: suspend () -> HttpResponse
): ApiResult<T> {
    return try {
        val response = apiCall()
            
        when (response.status) {
            HttpStatusCode.OK -> {
                val type = response.contentType()
                val result = when {
                    type?.match(ContentType.Text.Plain) == true -> response.body<String>()
                    type?.match(ContentType.Application.Json) == true -> Json.decodeFromString<T>(
                        response.body()
                    )

                    else -> throw Exception("Unknown Content Type: $type")
                }

                ApiResult.Success(result as T)
            }

            HttpStatusCode.UnprocessableEntity,
            HttpStatusCode.Unauthorized,
            HttpStatusCode.BadRequest -> {
                val error = response.body<String>()
                val appError = Json.decodeFromString<AppError>(error)
                ApiResult.Error(appError)
            }

            else -> {
                ApiResult.Error(AppError(0, ErrorType.SERVER, "HTTP ${response.status.value}"))
            }
        }
    } catch (e: Exception) {
        ApiResult.NetworkError(e)
    }
}