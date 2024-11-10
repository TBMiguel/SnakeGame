package br.pucpr.snakegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent

class StartScene(private val screen: MainActivity.Screen): Scene {

    private val paint = Paint()
    private var controlTime = 0f

    init {
        paint.textSize = 70f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.color = Color.WHITE
    }

    override fun update(et: Float) {
        controlTime += et
        if (controlTime > 300) {
            controlTime = 0f
        }
    }

    override fun render(canvas: Canvas) {
        val title = "Snake Game"
        val text = "Toque para jogar"
        canvas.drawText(title, screen.width/2f, screen.height/3f, paint)
        canvas.drawText(text, screen.width/2f, screen.height/2f, paint)

    }

    override fun onTouch(e: MotionEvent): Boolean {
        return when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                screen.scene = SnakeGameScene(screen)
                true
            }
            else -> false
        }
    }
}