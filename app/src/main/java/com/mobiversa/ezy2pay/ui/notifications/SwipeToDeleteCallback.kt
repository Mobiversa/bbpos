package com.mobiversa.ezy2pay.ui.notifications

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


abstract class SwipeToDeleteCallback internal constructor(mContext: Context?) :
        ItemTouchHelper.Callback() {
    private val mClearPaint: Paint = Paint()
    private val mBackground: ColorDrawable = ColorDrawable()
    private val backgroundColor: Int = Color.parseColor("#EF5350")
    private val deleteDrawable: Drawable?
    private val readDrawable: Drawable?
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int


    init {
        mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
//        deleteDrawable = mContext?.let { ContextCompat.getDrawable(it, R.drawable.ic_delete_notification) }
//        readDrawable = mContext?.let { ContextCompat.getDrawable(it, R.drawable.read_swipe) }
        deleteDrawable = BitmapDrawable(mContext!!.resources,textAsBitmap("Delete",40F) )
        readDrawable = BitmapDrawable(mContext.resources,
            textAsBitmapRighht("Read",40F)?.rotate(0F)
        )
        intrinsicWidth = deleteDrawable.intrinsicWidth
        intrinsicHeight = deleteDrawable.intrinsicHeight


    }


    override fun getMovementFlags(recyclerView: RecyclerView,viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            viewHolder1: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView

        val isCancelled = dX == 0f && !isCurrentlyActive

        if (isCancelled) {
            clearCanvas(
                    c,
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

//        mBackground.color = backgroundColor

        // Swiping to the left
        val iconMargin = (itemView.height - deleteDrawable!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - deleteDrawable.intrinsicHeight) / 2
        val iconBottom = iconTop + deleteDrawable.intrinsicHeight

        // Swiping to the Right
        val readIconMargin = (itemView.height - readDrawable!!.intrinsicHeight) / 2
        val readIconTop = itemView.top + (itemView.height - readDrawable.intrinsicHeight) / 2
        val readIconBottom = iconTop + readDrawable.intrinsicHeight

        if (dX > 0) { // Swiping to the right
            val readIconLeft = itemView.left + readIconMargin + readDrawable.intrinsicWidth
            val readIconRight = itemView.left + readIconMargin
            readDrawable.setBounds(readIconLeft, readIconTop, readIconRight, readIconBottom)
            mBackground.color = Color.GREEN
            mBackground.setBounds(itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom)
        } else if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - deleteDrawable.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            deleteDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            mBackground.color = Color.RED
            mBackground.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top, itemView.right, itemView.bottom)
        } else { // view is unSwiped
            mBackground.setBounds(0, 0, 0, 0)
        }

        mBackground.draw(c)
        deleteDrawable.draw(c)
        readDrawable.draw(c)

//        val itemView = viewHolder.itemView
//        val itemHeight = itemView.height
//
//        val isCancelled = dX == 0f && !isCurrentlyActive
//
//        if (isCancelled) {
//            clearCanvas(
//                c,
//                itemView.right + dX,
//                itemView.top.toFloat(),
//                itemView.right.toFloat(),
//                itemView.bottom.toFloat()
//            )
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//            return
//        }
//
//        mBackground.color = backgroundColor
//        mBackground.setBounds(
//            itemView.right + dX.toInt(),
//            itemView.top,
//            itemView.right,
//            itemView.bottom
//        )
//        mBackground.draw(c)
//
//        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
//        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
//        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
//        val deleteIconRight = itemView.right - deleteIconMargin
//        val deleteIconBottom = deleteIconTop + intrinsicHeight
//
//
//        deleteDrawable!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
//        deleteDrawable.draw(c)
//
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)


    }

    private fun clearCanvas(c: Canvas, left: Float?, top: Float?, right: Float?, bottom: Float?) {
        c.drawRect(left!!, top!!, right!!, bottom!!, mClearPaint)

    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.7f
    }

    private fun textAsBitmap(text: String?, textSize: Float): Bitmap? {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0F, baseline, paint)
        return image
    }

    private fun textAsBitmapRighht(text: String?, textSize: Float): Bitmap? {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = textSize
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
        paint.isAntiAlias = true
        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        canvas.drawText(text!!, 0F, baseline, paint)
        return image
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}