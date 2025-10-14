package com.promtuz.chat.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ErrorType {
    @SerialName("FORM")
    FORM,

    @SerialName("AUTH")
    AUTH,

    @SerialName("NOT_FOUND")
    NOT_FOUND,

    @SerialName("SERVER")
    SERVER,

    @SerialName("CONFLICT")
    CONFLICT,

    @SerialName("BAD_REQUEST")
    BAD_REQUEST
}

@Serializable
data class FieldError(
    val name: String,
    val error: String
)

@Serializable
data class AppError(
    val ok: Int,
    val type: ErrorType,
    val error: String? = null,
    val fields: List<FieldError>? = null
)

// For successful responses
@Serializable
data class ApiResponse<T>(
    val ok: Int = 1,
    val data: T? = null,
    val error: AppError? = null
)