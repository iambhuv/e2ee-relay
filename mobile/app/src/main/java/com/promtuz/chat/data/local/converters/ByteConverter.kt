package com.promtuz.chat.data.local.converters

import androidx.room.TypeConverter

class ByteConverter {
    @TypeConverter
    fun fromByteList(bytes: List<Byte>): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @TypeConverter
    fun toByteList(hex: String): List<Byte> {
        return hex.chunked(2).map { it.toInt(16).toByte() }
    }
}