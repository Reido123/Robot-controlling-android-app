package com.example.aplikacja_final

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class ManipulatorView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 3f
    }

    var l1: Double = 100.0
    var l2: Double = 100.0
    var theta1: Double = Math.toRadians(-90.0)
    var theta2: Double = Math.toRadians(90.0)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val startX = width / 2.0f
        val startY = height / 2.0f

        val x1 = startX + (l1 * cos(theta1)).toFloat()
        val y1 = startY + (l1 * sin(theta1)).toFloat()

        val x2 = x1 + (l2 * cos(theta1 + theta2)).toFloat()
        val y2 = y1 + (l2 * sin(theta1 + theta2)).toFloat()

        canvas.drawLine(startX, startY, x1, y1, paint)
        canvas.drawLine(x1, y1, x2, y2, paint)
    }

    fun updateAngles(theta1: Double, theta2: Double) {
        this.theta1 = theta1
        this.theta2 = theta2
        invalidate()
    }
}
