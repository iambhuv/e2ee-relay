package com.promtuz.chat.utils.serialization

import android.util.Log
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgAutoPackable

object AutoMsgPack {
    fun pack(obj: Any): ByteArray {
        val packer = MessagePack.newDefaultBufferPacker()
        packValue(packer, obj)
        return packer.toByteArray()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun packValue(packer: MessagePacker, value: Any?) {
        when (value) {
            null -> packer.packNil()
            is String -> packer.packString(value)
            is Int -> packer.packInt(value)
            is UByte -> packer.packByte(value.toByte())
            is Byte -> packer.packByte(value)
            is Long -> packer.packLong(value)
            is Boolean -> packer.packBoolean(value)
            is UByteArray -> {
                packer.packBinaryHeader(value.size)
                packer.writePayload(value.toByteArray())
            }

            is ByteArray -> {
                packer.packBinaryHeader(value.size)
                packer.writePayload(value)
            }

            is List<*> -> {
                if (value.isEmpty()) {
                    packer.packArrayHeader(0)
                } else {
                    packer.packArrayHeader(value.size)
                    value.forEach { packValue(packer, it) }
                }
            }

            else -> {
                val kClass = value::class

                val packable = kClass.findAnnotation<MsgAutoPackable>()

                if (packable == null) {
                    throw IllegalArgumentException(
                        "Class ${kClass.simpleName} must be annotated with @MsgPackable. " +
                                "All nested data classes must have @MsgPackable annotation."
                    )
                }

                packDataClass(packer, value)
            }
        }
    }

    private fun packDataClass(packer: MessagePacker, obj: Any) {
        val kClass = obj::class
        val properties = kClass.memberProperties

        val inline = kClass.findAnnotation<JvmInline>()

        if (inline == null) packer.packArrayHeader(properties.size)
        properties.forEach { prop ->
            @Suppress("UNCHECKED_CAST")
            val value = (prop as KProperty1<Any, *>).get(obj)
            packValue(packer, value)
        }
    }
}