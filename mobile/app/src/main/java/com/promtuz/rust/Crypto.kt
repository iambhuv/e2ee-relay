package com.promtuz.rust

import com.promtuz.chat.data.remote.dto.Bytes
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
data class EncryptedData(
    val nonce: Bytes, val cipher: Bytes
) {
    override fun toString(): String {
        return "EncryptedData {\n   " +
                "   nonce: ${this.nonce.bytes.toHexString()}\n   " +
                "   cipher: ${this.cipher.bytes.toHexString()}\n   " +
                "}"
    }
}

class Crypto : KoinComponent {
    init {
        // Forces Dependency on Core
        inject<Core>()
    }

    /**
     * returns the pointer to `EphemeralSecret Key` and `Ephemeral Public Key Bytes`
     */
    external fun getEphemeralKeypair(): Pair<Long, ByteArray>


    external fun ephemeralDiffieHellman(
        ephemeralSecretPtr: Long,
        publicKeyBytes: ByteArray
    ): ByteArray


    external fun diffieHellman(
        secretKeyBytes: ByteArray, publicKeyBytes: ByteArray
    ): ByteArray

    external fun deriveSharedKey(
        rawKey: ByteArray, salt: String, info: String
    ): ByteArray


    external fun decryptData(
        cipher: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        ad: ByteArray
    ): ByteArray

    external fun encryptData(
        data: ByteArray,
        key: ByteArray,
        ad: ByteArray
    ): EncryptedData
}