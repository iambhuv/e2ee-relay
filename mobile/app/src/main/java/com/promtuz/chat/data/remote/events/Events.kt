package com.promtuz.chat.data.remote.events

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

@Serializable
sealed class Events

/**
 * Events that are sent from app
 */
@Serializable
sealed class ClientEvents : Events()


/**
 * Events that are received by app
 */
@Serializable
sealed class ServerEvents : Events()


/**
 * Changes `{ EVENT : {...} }` to `["EVENT", ...]`
 */
fun eventize(packet: ByteArray): ByteArray {
    if (packet[0].toInt() and 0xF0 == 0xA0) {
        val count = packet[0].toInt() and 0x0F
        if (count == 1) { // Only if it's a 1-entry map
            val modified = packet.copyOf()
            modified[0] = 0x82.toByte() // Replace with array(2)
            return modified
        }
    }
    return packet
}