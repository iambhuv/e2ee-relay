package com.promtuz.chat.data.remote.events

import com.promtuz.chat.data.remote.dto.Bytes
import com.promtuz.chat.data.remote.dto.EncryptedData
import kotlinx.serialization.Serializable


object Server {
    @Serializable
    data class UnsafeHello(
        val epk: Bytes, val msg: EncryptedData
    )

    @Serializable
    enum class UnsafeReject {
        UnregisteredPublicKey, Unknown;

        companion object {
            fun from(value: String) = when (value) {
                "UnregisteredPublicKey" -> UnregisteredPublicKey
                else -> Unknown
            }
        }
    }
}


object Client {
    @Serializable
    data class HelloPayload(
        val ipk: Bytes, val epk: Bytes
    )
}
