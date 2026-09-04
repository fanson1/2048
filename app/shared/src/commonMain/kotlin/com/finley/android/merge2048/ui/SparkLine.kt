package com.finley.android.merge2048.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.finley.android.merge2048.GameColors

/**
 * Tiny line chart that draws a series of [points] as a smoothed curve. Renders
 * a baseline grid and the curve with a soft fill. Optimised for in-screen
 * history cards (e.g. last game's score curve, lifetime best scores).
 */
@Composable
fun SparkLine(
    points: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = GameColors.ButtonBackground,
    fillColor: Color = GameColors.ButtonBackground.copy(alpha = 0.18f),
    showGrid: Boolean = true
) {
    if (points.size < 2) {
        // Pad with a single zero so the canvas still draws a clean baseline.
        Box(modifier = modifier)
        return
    }

    val max = points.max().coerceAtLeast(1)
    val min = points.min().coerceAtMost(0)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val leftPad = 0f
        val rightPad = 0f
        val topPad = 4f
        val bottomPad = 4f
        val plotW = w - leftPad - rightPad
        val plotH = h - topPad - bottomPad

        // ---- Grid lines ----
        if (showGrid) {
            val grid = GameColors.SubText.copy(alpha = 0.18f)
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            for (i in 1..3) {
                val y = topPad + plotH * i / 4f
                drawLine(
                    color = grid,
                    start = Offset(leftPad, y),
                    end = Offset(w - rightPad, y),
                    strokeWidth = 1f,
                    pathEffect = dash
                )
            }
        }

        // ---- Map data points to pixel coordinates ----
        val coords = points.mapIndexed { i, value ->
            val x = leftPad + plotW * i / (points.size - 1)
            val yNormalized = if (max == min) 0.5f else (value - min).toFloat() / (max - min)
            val y = topPad + plotH * (1f - yNormalized)
            Offset(x, y)
        }

        // ---- Filled area below the curve ----
        val fill = Path().apply {
            moveTo(coords.first().x, topPad + plotH)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, topPad + plotH)
            close()
        }
        drawPath(path = fill, color = fillColor)

        // ---- Smooth curve (catmull-rom-ish via simple line segments) ----
        val line = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                val prev = coords[i - 1]
                val curr = coords[i]
                val midX = (prev.x + curr.x) / 2f
                cubicTo(
                    midX, prev.y,
                    midX, curr.y,
                    curr.x, curr.y
                )
            }
        }
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 3f)
        )

        // ---- Endpoint dot ----
        val last = coords.last()
        drawCircle(
            color = lineColor,
            radius = 4f,
            center = last
        )
    }
}