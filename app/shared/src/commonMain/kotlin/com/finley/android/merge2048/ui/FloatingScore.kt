package com.finley.android.merge2048.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.merge_combo_label_format
import merge2048.app.shared.generated.resources.merge_score_popup_format
import com.finley.android.merge2048.GameColors
import org.jetbrains.compose.resources.stringResource

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
        // Position within the board box based on grid coordinates.
        // Uses layout{} so the cell position is computed from the actual
        // parent size at layout time, not from a hardcoded dp assumption.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layout { measurable, constraints ->
                        val cellW = constraints.maxWidth / boardSize
                        val cellH = constraints.maxHeight / boardSize
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = cellW,
                                maxWidth = cellW,
                                minHeight = cellH,
                                maxHeight = cellH
                            )
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(col * cellW, row * cellH)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.merge_score_popup_format, points),
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
                            text = stringResource(Res.string.merge_combo_label_format, comboCount),
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
                text = stringResource(Res.string.merge_score_popup_format, points),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.ButtonBackground.copy(alpha = alpha),
                modifier = Modifier.offset(y = offsetY.dp)
            )
        }
    }
}
