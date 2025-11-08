package com.promtuz.chat.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class QrView(context: Context) : View(context) {

    var content = ByteArray(0)
    var sizePx = 0
    var color = Color.BLACK
    var cached: Bitmap? = null

    private val writer = QRCodeWriter()
    private val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "ISO-8859-1",
        EncodeHintType.MARGIN to 0
    )

    fun setBitmap(bmp: Bitmap?) {
        cached = bmp
        invalidate()
    }

    fun regenerate() {
        if (content.isEmpty() || sizePx <= 0) return

        // Step 1 — generate tiny QR (fast)
        val matrix = writer.encode(
            content.toString(Charsets.ISO_8859_1),
            BarcodeFormat.QR_CODE,
            0, 0,
            hints
        )

        val w = matrix.width
        val h = matrix.height

        val tiny = createBitmap(w, h)
        val black = color
        val white = Color.WHITE

        for (y in 0 until h) {
            for (x in 0 until w) {
                tiny[x, y] = if (matrix[x, y]) black else white
            }
        }

        // Step 2 — scale once (super fast)
        val scaled = tiny.scale(sizePx, sizePx, false)
        tiny.recycle()

        cached?.recycle()
        cached = scaled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        cached?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cached?.recycle()
        cached = null
    }
}
