package com.finley.android.merge2048.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors

/**
 * Merges at each merge position: shows "+N" floating upward, and a combo label
 * when combo > 1. Each popup is keyed on a unique combination of position + points
 * so animations restart on every move.
 */
@Composable
fun MergePopups(
    mergePositions: List<Triple<Int, Int, Int>>,
    comboCount: Int,
    boardSize: Int,
    modifier: Modifier = Modifier
) {
    if (mergePositions.isEmpty()) return

    Box(modifier = modifier) {
        mergePositions.forEachIndexed { index, (row, col, value) ->
            key("merge-$row-$col-$value-$index") {
                MergePopup(
                    points = value,
                    row = row,
                    col = col,
                    boardSize = boardSize,
                    isComboLabel = index == 0 && comboCount > 1,
                    comboCount = comboCount
                )
            }
        }
    }
}

@Composable
private fun MergePopup(
    points: Int,
    row: Int,
    col: Int,
    boardSize: Int,
    isComboLabel: Boolean,
    comboCount: Int
) {
    val transition = updateTransition(points, label = "merge-pop")

    val offsetY by transition.animateInt(
        label = "offset",
        transitionSpec = { tween(1000, easing = FastOutSlowInEasing) }
    ) { -50 }

    val alpha by transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            keyframes {
                durationMillis = 1000
                1f at 0
                1f at 250
                0f at 1000
            }
        }
    ) { 0f }

    val scale by transition.animateFloat(
        label = "scale",
        transitionSpec = {
            keyframes {
                durationMillis = 1000
                0f at 0
                1.3f at 80
                1f at 200
                1f at 1000
            }
        }
    ) { 0f }

    if (alpha > 0f) {
        // Position within the board box based on grid coordinates
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            // Compute fractional position within the board
            val cellFractionX = col.toFloat() / boardSize
            val cellFractionY = row.toFloat() / boardSize

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = 1f / boardSize)
                    .fillMaxHeight(fraction = 1f / boardSize)
                    .offset(
                        x = (cellFractionX * 100).toInt().dp,
                        y = (cellFractionY * 100).toInt().dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+$points",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GameColors.ButtonBackground.copy(alpha = alpha),
                        modifier = Modifier
                            .offset(y = offsetY.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                    if (isComboLabel && comboCount > 1) {
                        Text(
                            text = "COMBO x${comboCount}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GameColors.Tile2048.copy(alpha = alpha),
                            modifier = Modifier
                                .offset(y = (offsetY - 5).dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Legacy single-position floating score (kept for backward compatibility).
 */
@Composable
fun FloatingScore(
    points: Int,
    modifier: Modifier = Modifier
) {
    if (points <= 0) return

    val transition = updateTransition(points, label = "float")

    val offsetY by transition.animateInt(
        label = "offset",
        transitionSpec = { tween(1200, easing = FastOutSlowInEasing) }
    ) { -60 }

    val alpha by transition.animateFloat(
        label = "alpha",
        transitionSpec = {
            keyframes {
                durationMillis = 1200
                1f at 0
                1f at 300
                0f at 1200
            }
        }
    ) { 0f }

    if (alpha > 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "+$points",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.ButtonBackground.copy(alpha = alpha),
                modifier = Modifier.offset(y = offsetY.dp)
            )
        }
    }
}
