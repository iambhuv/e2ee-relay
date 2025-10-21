package com.promtuz.chat.data.remote.realtime

import android.util.Log
import com.promtuz.chat.data.remote.dto.Bytes
import com.promtuz.chat.data.remote.events.Client
import com.promtuz.chat.data.remote.events.Server
import com.promtuz.chat.data.remote.framePacket
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Crypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import tech.kwik.core.QuicClientConnection
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
        Cbor.decodeFromByteArray<T>(bytes)
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: SerializationException) {
        null
    }
}

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalSerializationApi::class)
class Handshake(
    keyManager: KeyManager, crypto: Crypto, conn: QuicClientConnection
) {
    private var keyPair: EphemeralKeyPair
    private var serverEPK: Bytes? = null
    private var stream = conn.createStream(true)


    val serverPublicKey
        get(): Bytes? {
            return this.serverEPK
        }

    val ephemeralKeyPair
        get(): EphemeralKeyPair? {
            return this.keyPair
        }

    private fun listener() {
        while (true) {
            try {
                val recv = stream.inputStream

                val packetSizeBuffer = recv.readNBytes(4)
                val packetSize =
                    ByteBuffer.wrap(packetSizeBuffer).order(ByteOrder.BIG_ENDIAN).int.toUInt()

                val packet = recv.readNBytes(packetSize.toInt())

                Log.d("Handshake", "Got Packet : ${packet.toHexString()}")

                when (val msg = cborDecode<Server.UnsafeHello>(packet)
                    ?: cborDecode<Server.UnsafeReject>(packet)) {
                    is Server.UnsafeHello -> {
                        Log.e(
                            "Handshake",
                            "Got UnsafeServerHello packet : UnsafeServerHello {" + "   epk: ${msg.epk.bytes.toHexString()}" + "   msg: ${msg.msg}" + "}"
                        )
                    }

                    is Server.UnsafeReject -> {
                        Log.e("Handshake", "Handshake Rejected with Reason $msg")
                    }

                    null -> {}
                }

            } catch (e: IOException) {
                Log.e("Handshake", "QUIC Handshake Failed:", e)
                break
            }
        }
    }

    init {

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            listener()
        }

        val keyPair = crypto.getEphemeralKeypair();

        this.keyPair = EphemeralKeyPair(keyPair.first, Bytes(keyPair.second))

        keyManager.getPublicKey()?.let { identityPublicKey ->
            val hello = Client.HelloPayload(
                Bytes(identityPublicKey), Bytes(this.keyPair.epk.bytes)
            )

            val send = stream.outputStream

            val payload = Cbor.encodeToByteArray(hello)

            val decoded = Cbor.decodeFromByteArray<Client.HelloPayload>(payload)
            println(decoded.epk.bytes.toList())

            println("Sending Payload : ${payload.toHexString()}")

            send.write(framePacket(payload))
            send.flush()
        }
    }
}