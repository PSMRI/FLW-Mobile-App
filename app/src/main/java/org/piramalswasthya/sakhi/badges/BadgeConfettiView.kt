package org.piramalswasthya.sakhi.badges

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Random

/**
 * Dependency-free confetti burst for badge celebrations: coloured pieces
 * fountain up from the centre and fall with gravity, fading at the end.
 */
class BadgeConfettiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private class Piece(
        val x0: Float, val y0: Float, val vx: Float, val vy: Float,
        val color: Int, val size: Float, val spin: Float
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pieces: List<Piece> = emptyList()
    private var t = 0f
    private var animator: ValueAnimator? = null

    fun burst() {
        if (width == 0) {
            post { burst() }
            return
        }
        val rnd = Random()
        pieces = List(90) {
            Piece(
                x0 = width / 2f,
                y0 = height * 0.45f,
                vx = (rnd.nextFloat() - 0.5f) * width * 2.2f,
                vy = -(height * (0.6f + rnd.nextFloat() * 1.1f)),
                color = COLORS[rnd.nextInt(COLORS.size)],
                size = 8f + rnd.nextFloat() * 14f,
                spin = (rnd.nextFloat() - 0.5f) * 720f
            )
        }
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2400
            addUpdateListener {
                t = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (t <= 0f || pieces.isEmpty()) return
        val gravity = height * 2.2f
        paint.alpha = (255 * (1f - t * t)).toInt().coerceIn(0, 255)
        for (p in pieces) {
            val x = p.x0 + p.vx * t
            val y = p.y0 + p.vy * t + 0.5f * gravity * t * t
            if (y > height + p.size) continue
            paint.color = p.color
            paint.alpha = (255 * (1f - t * t)).toInt().coerceIn(0, 255)
            canvas.save()
            canvas.rotate(p.spin * t, x, y)
            canvas.drawRoundRect(
                x - p.size / 2, y - p.size / 4, x + p.size / 2, y + p.size / 4,
                3f, 3f, paint
            )
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    companion object {
        private val COLORS = intArrayOf(
            0xFFF44336.toInt(), 0xFFFF9800.toInt(), 0xFFFFEB3B.toInt(),
            0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFF9C27B0.toInt(),
            0xFF00BCD4.toInt(), 0xFFFFD700.toInt()
        )
    }
}
