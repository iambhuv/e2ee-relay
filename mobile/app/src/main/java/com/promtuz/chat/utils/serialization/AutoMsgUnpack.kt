package com.promtuz.chat.utils.serialization

import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MsgAutoUnpackable


object AutoMsgUnpack {
    inline fun <reified O : Any> unpack(bytes: ByteArray?): Any? {
        val kClass = O::class

        kClass.annotations.find { it is MsgAutoUnpackable }
            ?: throw IllegalArgumentException("${kClass.simpleName} must have @MsgAutoUnpackable annotation")

        bytes ?: return null

//        print("Obj Properties : ")

//        println()

        val unpacker = MessagePack.newDefaultUnpacker(bytes)

        kClass.memberProperties.forEach {
            val type = it.returnType.classifier as? KClass<*>


        }

        TODO()
    }


    private fun unpackValue(unpacker: MessageUnpacker, type: KClass<*>) {
        when (type) {
            String::class -> {
                unpacker.unpackString()
            }

            else -> {}
        }
    }
}