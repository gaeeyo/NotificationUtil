package jp.syoboi.android.notificationutil

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class PopupView(context: Context?, val lineCount: Int, val scrollInterval: Long)
    : View(context) {

    val SCROLL_ANIMATION_DURATION = 200
    val SCROLL_IP = DecelerateInterpolator()

    val newLines = ArrayList<CharSequence>()
    val lines = arrayOfNulls<Line?>(lineCount + 1)
    val paint = TextPaint()
    val lineHeight: Int

    var scrollStartTime: Long = 0
    var showBorder: Boolean = false

    init {
        paint.textSize = 12f * resources.displayMetrics.density
        paint.isAntiAlias = true

        val fm = paint.fontMetricsInt
        lineHeight = fm.bottom - fm.top
    }


    var index = 0

    fun addLog(message: CharSequence) {
        newLines.add(message)
        index++
        invalidate()

        if (width > 0) {
            if (lines.all { it == null }) {
                scrollNext()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (width > 0) {
            if (lines.all { it == null }) {
                scrollNext()
            }
        }
    }


    fun scrollNext() {
        for (j in 0..lines.lastIndex - 1) {
            lines[j] = lines[j + 1]
        }
        lines[lines.lastIndex] = popNewLine()

        scrollStartTime = SystemClock.elapsedRealtime()
        invalidate()
        if (lines.any
        { it != null }) {
            postDelayed(scrollRunnable, scrollInterval)
        }
    }

    fun popNewLine(): Line? {
        if (newLines.size == 0) {
            return null
        }

        val newLine = newLines.get(0)

        val widths = FloatArray(newLine.length)
        paint.getTextWidths(newLine, 0, newLine.length, widths)

        val layoutWidth = width
        val lineWidth: Int
        val lineText: CharSequence

        var overflow = false
        var width = 0f
        var splitPoint = 0
        var splitWidth = 0f
        for (j in 0..widths.lastIndex) {
            width += widths[j]
            if (width > layoutWidth) {
                if (splitPoint == 0) {
                    splitPoint = j
                    width -= widths[j]
                } else {
                    width = splitWidth
                }
                overflow = true
                break
            }
            if (newLine[j] == ' ') {
                if (j + 1 <= widths.lastIndex) {
                    splitPoint = j + 1
                } else {
                    splitPoint = j
                }
                splitWidth = width
            }
        }

        if (overflow) {
            newLines[0] = newLine.subSequence(splitPoint, newLine.length)
            lineWidth = width.toInt()
            lineText = newLine.subSequence(0, splitPoint)
        } else {
            newLines.removeAt(0)
            lineText = newLine
            lineWidth = width.toInt()
        }
        return Line(lineText, lineWidth)
    }

    val scrollRunnable = object : Runnable {
        override fun run() {
            scrollNext()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        if (showBorder) {
            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            canvas.drawRect(0f, 0f, width.toFloat(), lineCount.toFloat() * lineHeight, paint)
        }

        val ellapsed = SystemClock.elapsedRealtime() - scrollStartTime

        val scrollOffset: Float
        if (ellapsed >= SCROLL_ANIMATION_DURATION) {
            scrollOffset = lineHeight.toFloat()
        } else {
            scrollOffset = SCROLL_IP.getInterpolation(ellapsed.toFloat() / SCROLL_ANIMATION_DURATION) * lineHeight
            postInvalidate()
        }

        canvas.clipRect(0, 0, width, lineCount * lineHeight)
        canvas.translate(0f, -scrollOffset)
        paint.style = Paint.Style.FILL

        val fm = paint.fontMetricsInt
        for (j in 0..lines.lastIndex) {
            val y = j * lineHeight
            val line = lines[j]
            if (line != null) {
                paint.color = 0xaa000000.toInt()
                canvas.drawRect(0f, y.toFloat(),
                        line.width.toFloat(), (y + lineHeight).toFloat(), paint)
                paint.color = Color.WHITE
                canvas.drawText(line.text, 0, line.text.length, 0f,
                        y.toFloat() - fm.top, paint)
            }
        }
    }
}

class Line(val text: CharSequence, val width: Int)