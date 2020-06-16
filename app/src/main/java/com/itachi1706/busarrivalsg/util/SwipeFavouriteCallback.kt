package com.itachi1706.busarrivalsg.util

import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.itachi1706.busarrivalsg.R
import com.itachi1706.helperlib.utils.BitmapUtil

/**
 * Created by Kenneth on 10/12/2019.
 * for com.itachi1706.busarrivalsg.util in SingBuses
 */
open class SwipeFavouriteCallback(var context: Context, var callback: ISwipeCallback, dragDir: Int = 0) :
        ItemTouchHelper.SimpleCallback(dragDir, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    constructor(context: Context, callback: ISwipeCallback) : this(context, callback, 0)

    private val favourite: Int = Color.GREEN
    private val unfavourite: Int = Color.RED
    private val favouriteBitmap: Bitmap = BitmapUtil.getBitmap(context, R.drawable.ic_favourite)!!
    private val unfavouriteBitmap: Bitmap = BitmapUtil.getBitmap(context, R.drawable.ic_favourte_off)!!
    private val iconColor: Paint = Paint().apply { color = Color.WHITE; colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }

    interface ISwipeCallback {
        fun toggleFavourite(position: Int): Boolean
        fun moveFavourite(oldPosition: Int, newPosition: Int): Boolean
        fun getFavouriteState(position: Int): Boolean
    }

    // No movement
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback.toggleFavourite(viewHolder.adapterPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val swipeDirection = if (dX > 0) ItemTouchHelper.RIGHT else if (dX < 0) ItemTouchHelper.LEFT else ItemTouchHelper.UP

            val isFav = callback.getFavouriteState(viewHolder.adapterPosition)
            val selectedBackground = if (isFav) unfavourite else favourite
            val selectedBitmap = if (isFav) unfavouriteBitmap else favouriteBitmap

            val height = itemView.bottom.toFloat() - itemView.top.toFloat()
            val width = height / 3
            val p = Paint().apply { color = selectedBackground }

            when (swipeDirection) {
                ItemTouchHelper.RIGHT -> {
                    val bg = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
                ItemTouchHelper.LEFT -> {
                    val bg = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    c.drawRect(bg, p)
                    val iconDest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                    c.drawBitmap(selectedBitmap, null, iconDest, iconColor)
                }
            }

        }
    }
}