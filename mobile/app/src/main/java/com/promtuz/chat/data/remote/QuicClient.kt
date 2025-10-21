package com.promtuz.chat.data.remote

import android.util.Log
import com.promtuz.chat.data.remote.realtime.Handshake
import com.promtuz.chat.security.KeyManager
import com.promtuz.rust.Core
import com.promtuz.rust.Crypto
import io.ktor.http.parametersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import tech.kwik.core.QuicClientConnection
import tech.kwik.core.QuicConnection
import java.net.URI
import java.time.Duration

fun u32size(len: Int): ByteArray {
    val size: UInt = len.toUInt()
    val sizeBytes = ByteArray(4)
    sizeBytes[0] = (size shr 24).toByte()
    sizeBytes[1] = (size shr 16).toByte()
    sizeBytes[2] = (size shr 8).toByte()
    sizeBytes[3] = size.toByte()
    return sizeBytes
}

fun framePacket(packet: ByteArray): ByteArray {
    return u32size(packet.size) + packet
}

class QuicClient(private val keyManager: KeyManager, private val crypto: Crypto) : KoinComponent {
    //    private val addr = Pair("arch.local", 4433)
    private val addr = Pair("192.168.100.137", 4433)
    private var connection: QuicClientConnection? = null

    private lateinit var handshake: Handshake

    fun connect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (connection !== null) {
                    connection?.close(1001, "Reinitiating Connection")
                }

                connection =
                    QuicClientConnection.newBuilder().version(QuicConnection.QuicVersion.V1)
                        .uri(URI("https://${addr.first}:${addr.second}"))
                        .applicationProtocol("ProtoCall")
                        .noServerCertificateCheck()
                        .maxIdleTimeout(Duration.ofHours(1))
                        .build()

                connection?.connect()
                connection?.let {
                    println("Connection ? $it")
                    handshake = Handshake(get(), get(), it)
                }

            } catch (e: Exception) {
                Log.e("QuicClient", "Failed to Connect : $e")
            }
        }
    }
}