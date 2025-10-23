package com.promtuz.chat.utils.serialization

import com.promtuz.chat.data.remote.events.Events
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@OptIn(ExperimentalSerializationApi::class)
object AppCbor {
    val instance: Cbor = Cbor {
        ignoreUnknownKeys = true
        encodeObjectTags
        useDefiniteLengthEncoding = true
    }
}
