package com.example.rotatetonextpartview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF


val colors : Array<Int> = arrayOf(
    "#f44336",
    "#9C27B0",
    "#6200EA",
    "#00C853",
    "#795548"
).map {
    Color.parseColor(it)
}.toTypedArray()
val steps : Int = 4
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
    val size : Float = w / (steps + 1)
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    save()
    translate(0f, h / 2)
    var x : Float = size
    var currSf : Float = 1f
    for (j in 0..(steps - 1)) {
        val sfj : Float = sf.divideScale(j + 1, parts)
        if (sfj != 0f) {
            currSf *= sfj
        }
        x += size * Math.floor(sfj.toDouble()).toFloat()
    }
    if (sf1 >= 0f && sf1 < 1f) {
        currSf = 0f
    }
    val currSf1 : Float = currSf.divideScale(0, 2)
    val currSf2 : Float = currSf.divideScale(1, 2)
    save()
    translate(x, 0f)
    save()
    rotate(rot * currSf1)
    drawLine(-(size) * sf1, 0f, 0f, 0f, paint)
    restore()
    if (currSf > 0f) {
        drawArc(RectF(-size, -size, size, size), rot * (1 + currSf2), rot * (currSf1 - currSf2), true, paint)
    }
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

class RotateToNextPartView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RTNPNode(var i : Int, val state : State = State()) {

        private var next : RTNPNode? = null
        private var prev : RTNPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = RTNPNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRTNPNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RTNPNode {
            var curr : RTNPNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RotateToNextPart(var i : Int) {

        private var curr : RTNPNode = RTNPNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RotateToNextPartView) {

        private val animator : Animator = Animator(view)
        private val rtnp : RotateToNextPart = RotateToNextPart(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            rtnp.draw(canvas, paint)
            animator.animate {
                rtnp.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rtnp.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RotateToNextPartView {
            val view : RotateToNextPartView = RotateToNextPartView(activity)
            activity.setContentView(view)
            return view
        }
    }
}