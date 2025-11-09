package com.promtuz.chat.domain.model

import com.promtuz.chat.ui.activities.ShareIdentity
import com.promtuz.chat.utils.extensions.then
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val QR_MAGIC_NUMBER: UInt = 0x0750545au

/**
 *  Identity data class is used in exchanging public keys using QR on [ShareIdentity]
 *  Same can be used for both providing and retrieving information
 *
 *  Identity will contain PublicKey AgreementToken Nickname?
 *
 *  AgreementToken can be an array of 4 null bytes to indicate a non agreement qr
 * ```
 * ┌───────────────┬────────────────┬────────────────┐
 * ├───────────────┤ Byte Structure ├────────────────┤
 * ├────────┬──────┼──────────┬─────┴────────────────┤
 * │ Offset │ Size │ Type     │ Description          │
 * ├────────┼──────┼──────────┼──────────────────────┤
 * │ 0x00   │ 4    │ uint32   │ Magic number         │
 * │ 0x04   │ 32   │ uint8[]  │ Identity Public Key  │
 * │ 0x24   │ 4|32 │ uint8[]  │ Agreement Token      │
 * │ END    │ var  │ uint8[]  │ Optional Nickname    │
 * └────────┴──────┴──────────┴──────────────────────┘
 * ```
 */
data class Identity(
    val key: ByteArray,
    val nickname: String = "",
    val token: ByteArray? = null,
) {
    init {
        require(key.size == 32) { "Identity Public Key must be 32 bytes" }
        require(token == null || token.size == 32) { "Agreement Token must be 32 bytes" }
    }

    /**
     * Generates a QR Ready ByteArray
     */
    fun toByteArray(): ByteArray {
        val nicknameBytes = nickname.toByteArray(Charsets.UTF_8)
        val minSize = if (token != null) MIN_SIZE_TOKEN else MIN_SIZE
        val bufferSize = minSize + nicknameBytes.size

        val buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(QR_MAGIC_NUMBER.toInt())
        buffer.put(this.key)

        buffer.put(token ?: ByteArray(4))

        buffer.put(nicknameBytes)

        return buffer.array()
    }


    companion object {
        private const val MIN_SIZE = 4 + 32 + 4
        private const val MIN_SIZE_TOKEN = 4 + 32 + 32

        /**
         * Caching is important to prevent unnecessary computation & allocations when scanning qr
         */
        private val identityCache = mutableMapOf<Int, Identity?>()

        /**
         * Converts ByteArray to Identity class
         */
        fun fromByteArray(bytes: ByteArray): Identity? {
            return identityCache.getOrPut(bytes.contentHashCode()) {
                (bytes.size < MIN_SIZE).then { return null }

                val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

                try {
                    (buffer.getInt().toUInt() != QR_MAGIC_NUMBER).then { return null }

                    val key = ByteArray(32)
                    buffer.get(key)

                    val token: ByteArray? = if (isNullToken(buffer)) {
                        null
                    } else {
                        val tokenBuf = ByteArray(32)
                        buffer.get(tokenBuf)
                        tokenBuf
                    }

                    val nickname = if (buffer.hasRemaining()) {
                        val nicknameBytes = ByteArray(buffer.remaining())
                        buffer.get(nicknameBytes)
                        String(nicknameBytes, Charsets.UTF_8)
                    } else {
                        ""
                    }

                    Identity(key, nickname, token)
                } catch (_: Exception) {
                    null
                }
            }
        }

        private fun isNullToken(buffer: ByteBuffer): Boolean {
            val startPosition = buffer.position()
            val nullToken = ByteArray(4)
            buffer.get(nullToken)
            return nullToken.all { byte -> byte.toInt() == 0x00 }.also { if (!it) buffer.position(startPosition) }
        }
    }

    override fun equals(other: Any?) =
        other is Identity &&
                key.contentEquals(other.key) &&
                nickname.contentEquals(other.nickname) &&
                token.contentEquals(other.token)

    override fun hashCode() =
        31 * key.contentHashCode() +
            (nickname.hashCode()) +
            (token.contentHashCode())
}