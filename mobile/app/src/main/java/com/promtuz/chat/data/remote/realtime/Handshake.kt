package com.promtuz.chat.data.remote.realtime

import com.promtuz.chat.data.remote.ConnectionError
import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.data.remote.dto.Bytes
import com.promtuz.chat.data.remote.events.Client
import com.promtuz.chat.data.remote.events.ConnectionEvents
import com.promtuz.chat.data.remote.events.Server
import com.promtuz.chat.data.remote.events.ServerEvents
import com.promtuz.chat.data.remote.events.eventize
import com.promtuz.chat.data.remote.framePacket
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.rust.Crypto
import com.promtuz.rust.EncryptedData
import com.promtuz.rust.Info
import com.promtuz.rust.Salts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *
 * 1. Handshake needs Client::{KP}
 * - `Core.getEphemeralKeypair()`
 *
 */

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> cborDecode(bytes: ByteArray): T? {
    return try {
        AppCbor.instance.decodeFromByteArray<T>(bytes)
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)
class Handshake(
    private val keyManager: KeyManager,
    private val crypto: Crypto,
    private val quicClient: QuicClient
) {
    private lateinit var keyPair: EphemeralKeyPair
    private lateinit var sharedSecret: ByteArray

    private var serverEphemeralPublicKey: Bytes? = null
    private var stream = quicClient.connection?.createStream(true)


    /**
     * Associated Data for AEAD
     */
    private object AD {
        var hello: Bytes? = null
    }

    val serverPublicKey
        get(): Bytes? {
            return this.serverEphemeralPublicKey
        }

    val ephemeralKeyPair
        get(): EphemeralKeyPair? {
            return this.keyPair
        }

    private suspend fun listener(): ByteArray = withContext(Dispatchers.IO) {
        val recv = stream?.inputStream ?: throw IOException("No stream")
        while (recv.available() != -1) {
            val packet = recv.readNBytes(
                ByteBuffer.wrap(recv.readNBytes(4))
                    .order(ByteOrder.BIG_ENDIAN).int
            )

            when (val data = cborDecode<Server.UnsafeHello>(packet)
                ?: cborDecode<Server.UnsafeReject>(packet)
                ?: cborDecode<EncryptedData>(packet)) {
                is Server.UnsafeHello -> handleServerHello(data)
                is Server.UnsafeReject -> throw ConnectionError.HandshakeFailed("Rejected: $data")
                is EncryptedData -> when (decodeServerEvent(data)) {
                    is ConnectionEvents.Accept -> {
                        recv.close()
                        return@withContext sharedSecret
                    }

                    null -> {}
                }
            }
        }

        throw ConnectionError.HandshakeFailed("Stream Closed")
    }

    suspend fun initialize(): ByteArray = coroutineScope {
        val keyPair = crypto.getEphemeralKeypair();

        this@Handshake.keyPair = EphemeralKeyPair(keyPair.first, Bytes(keyPair.second))

        val identityPublicKey = keyManager.getPublicKey()
            ?: throw IOException("Identity Public Key Unavailable in KeyManager")

        val hello = Client.HelloPayload(
            Bytes(identityPublicKey), Bytes(this@Handshake.keyPair.epk.bytes)
        )

        val payload = AppCbor.instance.encodeToByteArray(hello)
        AD.hello = Bytes(payload)

        stream?.outputStream?.write(framePacket(payload))
        stream?.outputStream?.flush()

        listener()
    }


    private fun handleServerHello(data: Server.UnsafeHello) {
        this.serverEphemeralPublicKey = data.epk

        val isk = keyManager.getSecretKey() as ByteArray

        val proof = crypto.decryptData(
            data.msg.cipher.bytes, data.msg.nonce.bytes, crypto.deriveSharedKey(
                crypto.diffieHellman(isk, data.epk.bytes),
                Salts.HANDSHAKE,
                Info.SERVER_HANDSHAKE_SV_TO_CL
            ), (AD.hello as Bytes).bytes
        )

        this.sharedSecret = crypto.ephemeralDiffieHellman(this.keyPair.esk, data.epk.bytes)

        val payload = quicClient.prepareMsg(
            ConnectionEvents.Connect(Bytes(proof)), sharedSecret = sharedSecret
        )

        stream?.outputStream?.write(framePacket(payload))
        stream?.outputStream?.flush()
    }


    private fun decodeServerEvent(data: EncryptedData): ServerEvents? {
        return cborDecode<ServerEvents>(
            eventize(
                crypto.decryptData(
                    data.cipher.bytes, data.nonce.bytes, crypto.deriveSharedKey(
                        sharedSecret, Salts.EVENT, Info.SERVER_EVENT_SV_TO_CL
                    ), ByteArray(0)
                )
            )
        )
    }
}