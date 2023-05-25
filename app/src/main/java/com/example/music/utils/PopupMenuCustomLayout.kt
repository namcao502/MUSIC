package com.example.music.utils

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow

class PopupMenuCustomLayout(
    context: Context,
    rLayoutId: Int,
    onClickListener: PopupMenuCustomOnClickListener
) {
    private val onClickListener: PopupMenuCustomOnClickListener
    private val context: Context
    private val popupWindow: PopupWindow
    private val rLayoutId: Int
    private val popupView: View

    init {
        this.context = context
        this.onClickListener = onClickListener
        this.rLayoutId = rLayoutId
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(rLayoutId, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.elevation = 10f
        val linearLayout = popupView as LinearLayout
        for (i in 0 until linearLayout.childCount) {
            val v: View = linearLayout.getChildAt(i)
            v.setOnClickListener { v1 ->
                onClickListener.onClick(v1.id)
                popupWindow.dismiss()
            }
        }
    }

    fun setAnimationStyle(animationStyle: Int) {
        popupWindow.animationStyle = animationStyle
    }

    fun show() {
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    fun show(anchorView: View, gravity: Int, offsetX: Int, offsetY: Int) {
        popupWindow.showAsDropDown(anchorView, 0, -2 * anchorView.height)
    }

    interface PopupMenuCustomOnClickListener {
        fun onClick(menuItemId: Int)
    }
}