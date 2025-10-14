package com.promtuz.chat.data.remote.api

import com.promtuz.chat.data.remote.dto.AppError

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val appError: AppError) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
}