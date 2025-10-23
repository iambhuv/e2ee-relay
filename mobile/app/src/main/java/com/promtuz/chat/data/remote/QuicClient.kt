package com.promtuz.chat.data.remote

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import com.promtuz.chat.data.remote.events.ClientEvents
import com.promtuz.chat.data.remote.events.ConnectionEvents
import com.promtuz.chat.data.remote.events.Server
import com.promtuz.chat.data.remote.events.ServerEvents
import com.promtuz.chat.data.remote.realtime.Handshake
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.rust.Crypto
import com.promtuz.rust.Info
import com.promtuz.rust.Salts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import tech.kwik.core.QuicClientConnection
import tech.kwik.core.QuicConnection
import java.net.InetSocketAddress
import java.net.Socket
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

sealed class ConnectionError {
    object NoInternet : ConnectionError()
    object ServerUnreachable : ConnectionError()
    object Timeout : ConnectionError()
    data class HandshakeFailed(val reason: String) : ConnectionError()
    data class Unknown(val exception: Exception) : ConnectionError()
}

class QuicClient(private val keyManager: KeyManager, private val crypto: Crypto) : KoinComponent {
    private val addr = Pair("192.168.100.137", 4433)
    var connection: QuicClientConnection? = null

    private lateinit var handshake: Handshake

    private lateinit var sharedSecret: ByteArray

    interface Listener {
        fun onConnectionSuccess()
        fun onConnectionFailure(e: ConnectionError)
    }

    private fun hasInternetConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun connect(context: Context, listener: Listener) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!hasInternetConnectivity(context)) {
                    return@launch listener.onConnectionFailure(ConnectionError.NoInternet)
                }

                if (connection !== null) {
                    connection?.close(1001, "Reinitiating Connection")
                }

                val conn =
                    QuicClientConnection.newBuilder().version(QuicConnection.QuicVersion.V1)
                        .uri(URI("https://${addr.first}:${addr.second}"))
                        .applicationProtocol("ProtoCall")
                        .noServerCertificateCheck()
                        .maxIdleTimeout(Duration.ofHours(1))
                        .build()

                connection = conn

                conn.connect()

                handshake = Handshake(get(), get(), this@QuicClient)
                handshake.initialize(object : Handshake.Listener {
                    override fun onHandshakeSuccess(sharedSecret: ByteArray) {
                        this@QuicClient.sharedSecret = sharedSecret
                        listener.onConnectionSuccess()
                    }

                    override fun onHandshakeReject(reason: Server.UnsafeReject) {
                        listener.onConnectionFailure(ConnectionError.HandshakeFailed("Connection Rejected : $reason"))

                        throw RuntimeException("Connection Rejected with Reason $reason")
                    }

                    override fun onHandshakeFailure(error: Exception) {
                        listener.onConnectionFailure(ConnectionError.Unknown(error))

                        throw RuntimeException("Connection Failed : ", error)
                    }
                })

            } catch (e: Exception) {
                listener.onConnectionFailure(ConnectionError.Unknown(e))

                Log.e("QuicClient", "Failed to Connect : $e")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun prepareMsg(
        ev: ClientEvents,
        ad: ByteArray = ByteArray(0),
        sharedSecret: ByteArray? = if (::sharedSecret.isInitialized) this.sharedSecret else null
    ): ByteArray {
        if (sharedSecret == null) {
            throw IOException("prepareMsg was called without proper shared key")
        }

        val key = crypto.deriveSharedKey(
            sharedSecret, Salts.EVENT, Info.CLIENT_EVENT_CL_TO_SV
        )

        val data = crypto.encryptData(AppCbor.instance.encodeToByteArray(ev), key, ad)

        return AppCbor.instance.encodeToByteArray(data)
    }
}