package br.pucpr.snakegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import kotlin.math.abs
import kotlin.random.Random

class SnakeGameScene(private val screen: MainActivity.Screen) : Scene {

    private val WIN_CONDITION = 80
    private val snakePaint = Paint().apply { color = Color.GREEN }
    private val foodPaint = Paint().apply { color = Color.RED }
    private val scorePaint = Paint().apply { color = Color.WHITE; textSize = 50f }

    private var startX = 0f
    private var startY = 0f
    private val moveInterval = 0.4f // 0.4 segundos entre os movimentos
    private var timeSinceLastMove = 0f // Acumula o tempo do ultimo movimento

    private val snakeSegments = ArrayList<Rect>()
    private var foodRect: Rect? = null
    private var score = 0
    private var direction = Direction.RIGHT
    private val minSwipeDistance = 100f // Distancia minima para detectar um deslize na tela

    init {
        snakeSegments.add(Rect(100, 100, 150, 150)) // inicia cobra e comida
        spawnFood()
    }

    override fun update(et: Float) {
        timeSinceLastMove += et
        if (timeSinceLastMove >= moveInterval) {
            moveSnake()
            timeSinceLastMove = 0f // reseta o temporizador
        }
        checkGameStatus()
    }

    override fun render(canvas: Canvas) {
        for (segment in snakeSegments) {
            canvas.drawRect(segment, snakePaint)
        }
        foodRect?.let { canvas.drawRect(it, foodPaint) }
        canvas.drawText("Pontuação: $score", 20f, screen.width/4f, scorePaint)
    }

    override fun onTouch(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = e.x
                startY = e.y
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = e.x
                val endY = e.y
                val deltaX = endX - startX
                val deltaY = endY - startY

                // Checa se o deslize foi longo o bastante
                if (Math.abs(deltaX) > minSwipeDistance || Math.abs(deltaY) > minSwipeDistance) {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        // Deslize horizontal
                        if (deltaX > 0 && direction != Direction.LEFT) {
                            direction = Direction.RIGHT
                            Log.d("SnakeGame", "Deslize detectado: DIREITA")
                        } else if (deltaX < 0 && direction != Direction.RIGHT) {
                            direction = Direction.LEFT
                            Log.d("SnakeGame", "Deslize detectado: ESQUERDA")
                        }
                    } else {
                        // Deslize Vertical
                        if (deltaY > 0 && direction != Direction.UP) {
                            direction = Direction.DOWN
                            Log.d("SnakeGame", "Deslize detectado: ACIMA")
                        } else if (deltaY < 0 && direction != Direction.DOWN) {
                            direction = Direction.UP
                            Log.d("SnakeGame", "Deslize detectado: ABAIXO")
                        }
                    }

                    // Reseta startX e startY após um gesto de passar o dedo válido (deslize)
                    startX = endX
                    startY = endY
                }
            }
        }
        return true
    }

    private fun spawnFood() {
        val x = Random.nextInt(0, screen.width - 100)
        val y = Random.nextInt(0, screen.height - 100)
        foodRect = Rect(x, y, x + 50, y + 50)
    }

    private fun moveSnake() {
        val head = snakeSegments[0] // Atual cabeça da cobra
        val newHead = when (direction) {
            Direction.UP -> Rect(head.left, head.top - 50, head.right, head.bottom - 50)
            Direction.DOWN -> Rect(head.left, head.top + 50, head.right, head.bottom + 50)
            Direction.LEFT -> Rect(head.left - 50, head.top, head.right - 50, head.bottom)
            Direction.RIGHT -> Rect(head.left + 50, head.top, head.right + 50, head.bottom)
        }

        // Insere o novo segmento de cabeça da cobra no início da lista
        snakeSegments.add(0, newHead)

        // Checa se a cobra comeu a comida e incrementa a pontuação
        if (newHead.intersect(foodRect!!)) {
            score += 10 // Aumenta pontuação
            spawnFood() // Spawna uma nova comida em uma localização randomica
        } else {
            // Se nenhum alimento for comido, remova o último segmento para simular o movimento
            snakeSegments.removeAt(snakeSegments.size - 1)
        }
    }

    private fun checkGameStatus() {
        val head = snakeSegments[0]

        // Checa se a cobra bateu na "parede"
        if (head.left < 0 || head.top < 0 || head.right > screen.width || head.bottom > screen.height) {
            endGame("Derrota! Você bateu na parede!")
        }

        // Checa se a cobra bateu nela mesma
        for (i in 1 until snakeSegments.size) {
            if (head.intersect(snakeSegments[i])) {
                endGame("Derrota! você bateu em você mesmo!")
                return
            }
        }

        // Verifique se há uma condição de vitória quando a pontuação chegar a 100
        if (score >= WIN_CONDITION) {
            endGame("Vitória! Você atingiu a meta de pontuação!")
        }
    }

    private fun endGame(message: String) {
        Toast.makeText(screen.context, message, Toast.LENGTH_SHORT).show()
        screen.scene = StartScene(screen)
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }
}