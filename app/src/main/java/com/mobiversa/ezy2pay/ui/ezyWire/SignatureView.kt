package com.mobiversa.ezy2pay.ui.ezyWire

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import kotlin.math.max
import kotlin.math.min


/**
 * Created by Sangavi K
 * For Mobiversa Sdn Bhd
 */

/**
 * The View where the signature will be drawn
 */
class SignatureView : View {
    private val paint = Paint()
    private val path = Path()
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val dirtyRect = RectF()
    var isSignatureDrawn = false
        private set

    fun init(context: Context?) {
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = STROKE_WIDTH
        // set the bg color as white
        setBackgroundColor(Color.WHITE)
        // width and height should cover the screen
        this.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
    }

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init(context)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(context)
    }// set the signature bitmap
    // important for saving signature

    /**
     * Get signature
     *
     * @return
     */
     val signature: Bitmap
         get() {
            var signatureBitmap: Bitmap? = null
            // set the signature bitmap
            if (signatureBitmap == null) {
                signatureBitmap =
                    Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565)
            }
            // important for saving signature
            val canvas = Canvas(signatureBitmap!!)
            draw(canvas)
            return signatureBitmap
        }

    /**
     * clear signature canvas
     */
    fun clearSignature() {
        path.reset()
        isSignatureDrawn = false
        this.invalidate()
    }

    // all touch events during the drawing
    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isSignatureDrawn = true
                path.moveTo(eventX, eventY)
                lastTouchX = eventX
                lastTouchY = eventY
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                resetDirtyRect(eventX, eventY)
                val historySize = event.historySize
                var i = 0
                while (i < historySize) {
                    val historicalX = event.getHistoricalX(i)
                    val historicalY = event.getHistoricalY(i)
                    expandDirtyRect(historicalX, historicalY)
                    path.lineTo(historicalX, historicalY)
                    i++
                }
                path.lineTo(eventX, eventY)
            }
            else -> return false
        }
        invalidate(
            (dirtyRect.left - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.top - HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.right + HALF_STROKE_WIDTH).toInt(),
            (dirtyRect.bottom + HALF_STROKE_WIDTH).toInt()
        )
        lastTouchX = eventX
        lastTouchY = eventY
        return true
    }

    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY
        }
    }

    private fun resetDirtyRect(eventX: Float, eventY: Float) {
        dirtyRect.left = lastTouchX.coerceAtMost(eventX)
        dirtyRect.right = lastTouchX.coerceAtLeast(eventX)
        dirtyRect.top = min(lastTouchY, eventY)
        dirtyRect.bottom = max(lastTouchY, eventY)
    }

    companion object {
        // set the stroke width
        private const val STROKE_WIDTH = 5f
        private const val HALF_STROKE_WIDTH = STROKE_WIDTH / 2
    }
}