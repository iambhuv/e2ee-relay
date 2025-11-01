package com.promtuz.chat.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrView(context: Context) : View(context) {
    private var content = ByteArray(0)
    private var size = 5
    private val writer = QRCodeWriter()
    private var color = Color.RED
    private var cachedBitmap: Bitmap? = null
    private var onQrGenerated: (() -> Unit)? = null

    private val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "ISO-8859-1",
        EncodeHintType.MARGIN to 5
    )

    private val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private var generationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun setOnQrGeneratedListener(listener: () -> Unit) {
        onQrGenerated = listener
    }

    fun setContent(bytes: ByteArray) {
        content = bytes
        generateBitmapAsync()
    }

    fun setSize(width: Int) {
        size = width
        generateBitmapAsync()
    }

    fun setColor(color: Int) {
        this.color = color
        generateBitmapAsync()
    }

    private fun generateBitmapAsync() {
        generationJob?.cancel()
        generationJob = scope.launch {
            if (content.isEmpty() || size <= 0) return@launch

            val bitMatrix = writer.encode(
                content.toString(Charsets.ISO_8859_1),
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            val bitmap = createBitmap(matrixWidth, matrixHeight)
            val canvas = Canvas(bitmap)

            val modulePaint = Paint().apply {
                color = this@QrView.color
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            val moduleSize = 1f

            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (bitMatrix[x, y]) {
                        canvas.drawRect(
                            x * moduleSize,
                            y * moduleSize,
                            (x + 1) * moduleSize,
                            (y + 1) * moduleSize,
                            modulePaint
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                cachedBitmap?.recycle()
                cachedBitmap = bitmap
                invalidate()
                onQrGenerated?.invoke()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        cachedBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        generationJob?.cancel()
        scope.cancel()
        cachedBitmap?.recycle()
        cachedBitmap = null
    }
}