package com.promtuz.chat.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.ByteString

@JvmInline
@Serializable
@OptIn(ExperimentalSerializationApi::class)
value class Bytes(@ByteString val bytes: ByteArray)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class EncryptedData(
    val nonce: Bytes, val cipher: Bytes
) {
    override fun toString(): String {
        return "EncryptedData {" + "   nonce: ${this.nonce.bytes.toHexString()}" + "   cipher: ${this.cipher.bytes.toHexString()}" + "}"
    }
}