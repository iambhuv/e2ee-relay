package com.promtuz.chat.utils.compression

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object ZlibCompressor {
    fun compress(jsonString: String): ByteArray {
        val deflater = Deflater()
        val input = jsonString.toByteArray(Charsets.UTF_8)
        deflater.setInput(input)
        deflater.finish()

        val buffer = ByteArray(1024)
        val output = ByteArrayOutputStream()

        while (!deflater.finished()) {
            val compressedSize = deflater.deflate(buffer)
            output.write(buffer, 0, compressedSize)
        }

        deflater.end()
        return output.toByteArray()
    }

    fun decompress(compressedData: ByteArray): String {
        val inflater = Inflater()
        inflater.setInput(compressedData)

        val buffer = ByteArray(1024)
        val output = ByteArrayOutputStream()

        while (!inflater.finished()) {
            val decompressedSize = inflater.inflate(buffer)
            output.write(buffer, 0, decompressedSize)
        }

        inflater.end()
        return output.toString(Charsets.UTF_8)
    }
}