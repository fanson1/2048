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

    // Play a single confetti burst (~3s); do not loop forever in the background.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(3000, easing = LinearEasing))
    }
    val p = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (particle in particles) {
            val currentX = (particle.x + particle.vx * p * 100f).coerceIn(0f, 1f) * w
            val currentY = ((particle.y + particle.vy * p * 100f) % 1.5f).coerceIn(0f, 1f) * h
            val alpha = if (currentY > h * 0.9f) {
                (1f - (currentY / h - 0.9f) * 10f).coerceIn(0f, 1f)
            } else {
                1f
            }

            drawCircle(
                color = particle.color.copy(alpha = alpha * 0.9f),
                radius = particle.size,
                center = Offset(currentX, currentY)
            )
        }
    }
}
