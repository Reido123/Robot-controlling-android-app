//package com.example.aplikacja_final
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.util.AttributeSet
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import kotlin.math.pow
//import kotlin.math.sqrt
//
//class Joystick @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr) {
//
//    var centerX = 0f
//    var centerY = 0f
//    private var baseRadius = 0f
//    private var circleRadius = 0f
//
//    private val circlePaint = Paint().apply {
//        color = Color.BLACK
//        style = Paint.Style.FILL_AND_STROKE
//    }
//
//    private val backgroundPaint = Paint().apply {
//        color = Color.GRAY
//        style = Paint.Style.FILL
//    }
//
//    private var joystickCallback: JoystickListener? = null
//
//    interface JoystickListener {
//        fun onJoystickMoved(xPercent: Float, yPercent: Float)
//        fun onJoystickReleased()
//    }
//
//    fun setJoystickListener(listener: JoystickListener) {
//        joystickCallback = listener
//    }
//
//    // Skalowanie rozmiaru joysticka
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        centerX = (w / 2).toFloat()
//        centerY = (h / 2).toFloat()
//        baseRadius = (w.coerceAtMost(h) / 3).toFloat()
//        circleRadius = (w.coerceAtMost(h) / 6).toFloat()
//    }
//
//    // Rysowanie joysticka i backgroundu
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
//        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint)
//    }
//
//    // Obsługa dotyku
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val displacement = sqrt(
//            (event.x - centerX).toDouble().pow(2.0) +
//                    (event.y - centerY).toDouble().pow(2.0)
//        )
//
//        if (event.action == MotionEvent.ACTION_UP) {
//            resetJoystick()
//            joystickCallback?.onJoystickMoved(0f, 0f)
//            joystickCallback?.onJoystickReleased() // Notify when joystick is released
//        } else {
//            updateJoystickPosition(event.x, event.y, displacement)
//            joystickCallback?.onJoystickMoved(
//                (centerX - width / 2) / (width / 2),
//                (centerY - height / 2) / (height / 2)
//            )
//        }
//        invalidate()
//        return true
//    }
//
//    private fun resetJoystick() {
//        centerX = width / 2f
//        centerY = height / 2f
//    }
//
//    private fun updateJoystickPosition(x: Float, y: Float, displacement: Double) {
//        // Zaktualizuj pozycję joysticka tak, aby poruszał się w granicach kwadratu
//        centerX = x.coerceIn(0f, width.toFloat())
//        centerY = y.coerceIn(0f, height.toFloat())
//        Log.e("lal",centerX.toString() )
//        Log.e("lal",centerY.toString() )
//    }
//}
//
//
//
//
package com.example.aplikacja_final

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt

class Joystick @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var joystickX = 0f
    var joystickY = 0f
    private var baseRadius = 0f
    private var circleRadius = 0f

    private val circlePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
    }

    private val backgroundPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private var joystickCallback: JoystickListener? = null

    interface JoystickListener {
        fun onJoystickMoved(xPercent: Float, yPercent: Float)
        fun onJoystickReleased()
    }

    fun setJoystickListener(listener: JoystickListener) {
        joystickCallback = listener
    }

    // Skalowanie rozmiaru joysticka
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetJoystick()
        baseRadius = (w.coerceAtMost(h) / 2).toFloat()
        circleRadius = (w.coerceAtMost(h) / 6).toFloat()
    }

    // Rysowanie joysticka i backgroundu
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Rysuj tło jako prostokąt
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        // Rysuj joystick jako okrąg
        canvas.drawCircle(joystickX, joystickY, circleRadius, circlePaint)
    }

    // Obsługa dotyku
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val displacement = sqrt(
            (event.x - width / 2).toDouble().pow(2.0) +
                    (event.y - height / 2).toDouble().pow(2.0)
        )

        if (event.action == MotionEvent.ACTION_UP) {
            resetJoystick()
            joystickCallback?.onJoystickMoved(50f, 50f)
            joystickCallback?.onJoystickReleased()
        } else {
            updateJoystickPosition(event.x, event.y, displacement)
            val scaledX = ((joystickX - width / 2) / baseRadius) * 30
            val scaledY = ((height / 2 - joystickY) / baseRadius) * 30
            Log.e("lal", scaledX.toString())
            Log.e("lal", scaledY.toString())
            joystickCallback?.onJoystickMoved(scaledX, scaledY)
        }
        invalidate()
        return true
    }

    private fun resetJoystick() {
        joystickX = width / 2f
        joystickY = height / 2f
    }

    private fun updateJoystickPosition(x: Float, y: Float, displacement: Double) {
        // Zaktualizuj pozycję joysticka tak, aby poruszał się w granicach koła
        if (displacement < baseRadius) {
            joystickX = x
            joystickY = y
        } else {
            val ratio = baseRadius / displacement
            joystickX = (width / 2) + (x - (width / 2)) * ratio.toFloat()
            joystickY = (height / 2) + (y - (height / 2)) * ratio.toFloat()
        }
    }
}
