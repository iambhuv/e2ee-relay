package com.promtuz.chat.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@JvmInline
@Serializable
@OptIn(ExperimentalSerializationApi::class)
value class Bytes(@ByteString val bytes: ByteArray)


@Serializable
enum class CaptchaReason {
    UnregisteredPublicKey,
    SuspiciousActivity
}

@Serializable
data class CaptchaRequest(
    val reason: CaptchaReason
)
