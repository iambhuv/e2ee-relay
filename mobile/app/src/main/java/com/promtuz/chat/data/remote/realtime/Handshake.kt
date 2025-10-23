package com.promtuz.chat.data.remote.realtime

import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
    val keyManager: KeyManager, val crypto: Crypto, val quicClient: QuicClient
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

    interface Listener {
        fun onHandshakeSuccess(sharedSecret: ByteArray)
        fun onHandshakeReject(reason: Server.UnsafeReject)
        fun onHandshakeFailure(error: Exception)
    }

    private fun listener(listener: Listener) {
        val recv = stream?.inputStream ?: return
        while (true) {
            try {
                val packetSizeBuffer = recv.readNBytes(4)
                val packetSize =
                    ByteBuffer.wrap(packetSizeBuffer).order(ByteOrder.BIG_ENDIAN).int.toUInt()

                val packet = recv.readNBytes(packetSize.toInt())

                Log.d("Handshake", "Got Packet : ${packet.toHexString()}")

                when (val data = cborDecode<Server.UnsafeHello>(packet)
                    ?: cborDecode<Server.UnsafeReject>(packet)
                    ?: cborDecode<EncryptedData>(packet)) {
                    is Server.UnsafeHello -> {
                        this.serverEphemeralPublicKey = data.epk

                        val isk = keyManager.getSecretKey() as ByteArray

                        Log.d(
                            "Handshake",
                            "Using AEAD(Hello) : ${(AD.hello as Bytes).bytes.toHexString()}"
                        )

                        val proof = crypto.decryptData(
                            data.msg.cipher.bytes,
                            data.msg.nonce.bytes,
                            crypto.deriveSharedKey(
                                crypto.diffieHellman(isk, data.epk.bytes),
                                Salts.HANDSHAKE,
                                Info.SERVER_HANDSHAKE_SV_TO_CL
                            ),
                            (AD.hello as Bytes).bytes
                        )

                        Log.d("Handshake", "Proof : ${proof.toHexString()}")

                        this.sharedSecret =
                            crypto.ephemeralDiffieHellman(this.keyPair.esk, data.epk.bytes)

                        val payload = quicClient.prepareMsg(
                            ConnectionEvents.Connect(Bytes(proof)),
                            sharedSecret = sharedSecret
                        )

                        Log.d("Handshake", "Sending Connect : ${payload.toHexString()}")

                        stream?.outputStream?.write(framePacket(payload))
                        stream?.outputStream?.flush()
                    }

                    is Server.UnsafeReject -> {
                        Log.e("Handshake", "Handshake Rejected with Reason $data")

                        listener.onHandshakeReject(data)
                    }

                    is EncryptedData -> {
                        when (cborDecode<ServerEvents>(
                            eventize(
                                crypto.decryptData(
                                    data.cipher.bytes,
                                    data.nonce.bytes,
                                    crypto.deriveSharedKey(
                                        sharedSecret,
                                        Salts.EVENT,
                                        Info.SERVER_EVENT_SV_TO_CL
                                    ),
                                    ByteArray(0)
                                )
                            )
                        )) {
                            is ConnectionEvents.Accept -> {
                                listener.onHandshakeSuccess(sharedSecret)
                            }

                            else -> {}
                        }
                    }

                    else -> {}
                }

            } catch (e: Exception) {
                Log.e("Handshake", "QUIC Handshake Failed:", e)
                listener.onHandshakeFailure(e)
                break
            }
        }
    }

    fun initialize(listener: Listener) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            listener(listener)
        }

        val keyPair = crypto.getEphemeralKeypair();

        this.keyPair = EphemeralKeyPair(keyPair.first, Bytes(keyPair.second))

        keyManager.getPublicKey()?.let { identityPublicKey ->
            val hello = Client.HelloPayload(
                Bytes(identityPublicKey), Bytes(this.keyPair.epk.bytes)
            )

            val payload = AppCbor.instance.encodeToByteArray(hello)
            AD.hello = Bytes(payload)

            stream?.outputStream?.write(framePacket(payload))
            stream?.outputStream?.flush()
        }
    }
}