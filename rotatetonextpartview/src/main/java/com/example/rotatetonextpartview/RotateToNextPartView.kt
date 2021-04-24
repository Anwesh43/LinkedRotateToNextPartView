package com.example.rotatetonextpartview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#9C27B0",
    "#6200EA",
    "#00C853",
    "#795548"
).map {
    Color.parseColor(it)
}.toTypedArray()
val steps : Int = 3
val parts : Int = 1 + steps
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val rot : Float = 180f
fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRotateToNextPart(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = w / steps
    val sf : Float = scale.sinify()
    save()
    translate(0f, h / 2)
    var x : Float = size / 2
    var currSf : Float = 1f
    for (j in 0..(steps - 1)) {
        val sfj : Float = sf.divideScale(j + 1, parts)
        if (sfj != 0f) {
            currSf *= sfj
        }
        x += size * Math.floor(sfj.toDouble()).toFloat()
    }
    save()
    translate(x, 0f)
    rotate(rot * currSf)
    drawLine(-size * sf.divideScale(0, parts), 0f, 0f, 0f, paint)
    restore()
    restore()
}

fun Canvas.drawRTNPNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawRotateToNextPart(scale, w, h, paint)
}
