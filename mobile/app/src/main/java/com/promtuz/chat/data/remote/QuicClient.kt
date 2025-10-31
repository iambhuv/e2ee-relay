package com.promtuz.chat.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.promtuz.chat.data.remote.events.ClientEvents
import com.promtuz.chat.data.remote.realtime.Handshake
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.serialization.AppCbor
import com.promtuz.rust.Crypto
import com.promtuz.rust.Info
import com.promtuz.rust.Salts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import tech.kwik.core.QuicClientConnection
import tech.kwik.core.QuicConnection
import timber.log.Timber
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

sealed class ConnectionError : Throwable() {
    object NoInternet : ConnectionError() {
        private fun readResolve(): Any = NoInternet
    }

    object ServerUnreachable : ConnectionError() {
        private fun readResolve(): Any = ServerUnreachable
    }

    object Timeout : ConnectionError() {
        private fun readResolve(): Any = Timeout
    }

    data class HandshakeFailed(val reason: String) : ConnectionError()
    data class Unknown(val exception: Exception) : ConnectionError()
}

enum class ConnectionStatus {
    Disconnected, Connecting, NetworkError, HandshakeFailed, Connected
}

class QuicClient(private val keyManager: KeyManager, private val crypto: Crypto) : KoinComponent {
    private val addr = Pair("arch.local", 4433)
    var connection: QuicClientConnection? = null

    private var _status = mutableStateOf(ConnectionStatus.Disconnected)
    val status: State<ConnectionStatus> get() = _status


    private lateinit var _handshake: Handshake
    val handshake: Handshake? get() = if (::_handshake.isInitialized) _handshake else null

    private lateinit var sharedSecret: ByteArray

    private fun hasInternetConnectivity(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    suspend fun connect(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasInternetConnectivity(context)) {
            return@withContext Result.failure(ConnectionError.NoInternet)
        }
        try {
            connection?.close(1001, "Reinitiating Connection")
            val conn = QuicClientConnection.newBuilder()
                .version(QuicConnection.QuicVersion.V1)
                .uri(URI("https://${addr.first}:${addr.second}"))
                .applicationProtocol("ProtoCall")
                .noServerCertificateCheck()
                .maxIdleTimeout(Duration.ofHours(1))
                .build()

            connection = conn
            _status.value = ConnectionStatus.Connecting
            conn.connect()

            _handshake = Handshake(get(), get(), this@QuicClient)
            sharedSecret = _handshake.initialize()
            _status.value = ConnectionStatus.Connected

            Result.success(Unit)
        } catch (e: Exception) {
            _status.value = ConnectionStatus.HandshakeFailed
            Timber.tag("QuicClient").d("Failed to Connect : $e")
            Result.failure(e)
        }
    }

    /**
     * @throws IOException if sharedSecret is null
     */
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