package com.finley.android.merge2048.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val rotationSpeed: Float,
    val life: Float // 0..1, 1 = full life
)

private val ConfettiColors = listOf(
    Color(0xFFF2B705), // gold
    Color(0xFFE94235), // red
    Color(0xFF2D9CDB), // blue
    Color(0xFF27AE60), // green
    Color(0xFF9B51E0), // purple
    Color(0xFFFF6B6B), // coral
    Color(0xFFFFE66D), // yellow
)

/**
 * Confetti celebration overlay. Shows falling confetti particles with
 * physics-based motion. Used when the player reaches 2048.
 */
@Composable
fun ConfettiCelebration(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -0.5f,
                vx = (Random.nextFloat() - 0.5f) * 0.003f,
                vy = Random.nextFloat() * 0.004f + 0.002f,
                size = Random.nextFloat() * 6f + 4f,
                color = ConfettiColors[Random.nextInt(ConfettiColors.size)],
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 8f,
                life = 1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti-progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val currentX = (p.x + p.vx * progress * 100f).coerceIn(0f, 1f) * w
            val currentY = ((p.y + p.vy * progress * 100f) % 1.5f).coerceIn(0f, 1f) * h
            val alpha = if (currentY > h * 0.9f) {
                (1f - (currentY / h - 0.9f) * 10f).coerceIn(0f, 1f)
            } else {
                1f
            }

            drawCircle(
                color = p.color.copy(alpha = alpha * 0.9f),
                radius = p.size,
                center = Offset(currentX, currentY)
            )
        }
    }
}
