package com.promtuz.chat.ui.components

import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrCode(
    data: ByteArray,
    modifier: Modifier = Modifier
) {
    val writer = QRCodeWriter()

    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "ISO-8859-1",
        EncodeHintType.MARGIN to 3
    )
    val qr =
        writer.encode(data.toString(Charsets.ISO_8859_1), BarcodeFormat.QR_CODE, 500, 500, hints)


    val bmp = createBitmap(qr.width, qr.height)

    for (x in 0..<qr.width) {
        for (y in 0..<qr.height) {
            bmp[x, y] = if (qr.get(x, y)) Color.BLACK else Color.WHITE
        }
    }

    Box(modifier
        .fillMaxWidth()
        .aspectRatio(1f)) {
        Image(bitmap = bmp.asImageBitmap(), "Identity Public QR Code", Modifier.fillMaxSize())
    }
}