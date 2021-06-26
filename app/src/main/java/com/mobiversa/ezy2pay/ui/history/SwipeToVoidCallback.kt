package com.mobiversa.ezy2pay.ui.history

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Align
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


abstract class SwipeToVoidCallback internal constructor(mContext: Context?) :
        ItemTouchHelper.Callback() {
    private val mClearPaint: Paint = Paint()
    private val mBackground: ColorDrawable = ColorDrawable()
    private val deleteDrawable: Drawable?
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int


    init {
        mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        deleteDrawable = BitmapDrawable(mContext!!.resources, textAsBitmap("Void",40F))
        intrinsicWidth = deleteDrawable.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight

    }


    override fun getMovementFlags( recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val position: Int = viewHolder.adapterPosition

        return makeMovementFlags(0, ItemTouchHelper.LEFT)
    }

    override fun onMove(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder,viewHolder1: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(c: Canvas,recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float,dY: Float,actionState: Int, isCurrentlyActive: Boolean ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView

        val isCancelled = dX == 0f && !isCurrentlyActive

        if (isCancelled) {
            clearCanvas(c,itemView.right + dX,itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Swiping to the left
        val iconMargin = (itemView.height - deleteDrawable!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - deleteDrawable.intrinsicHeight) / 2
        val iconBottom = iconTop + deleteDrawable.intrinsicHeight

        if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - deleteDrawable.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            deleteDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            mBackground.color = Color.RED
            mBackground.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom)
        }
        else { // view is unSwiped
            mBackground.setBounds(0, 0, 0, 0)
        }

        mBackground.draw(c)
        deleteDrawable.draw(c)

    }

    private fun clearCanvas(c: Canvas, left: Float?, top: Float?, right: Float?, bottom: Float?) {
        c.drawRect(left!!, top!!, right!!, bottom!!, mClearPaint)

    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }

    private fun textAsBitmap(text: String?, textSize: Float): Bitmap? {
        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = Color.WHITE
        paint.textAlign = Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0F, baseline, paint)
        return image
    }
}