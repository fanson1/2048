package com.finley.android.merge2048.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.MoveAnimationData
import com.finley.android.merge2048.ui.theme.GameColors
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.board_swipe_to_play
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val SWIPE_THRESHOLD_DP = 20f

private fun resolveSwipe(dx: Float, dy: Float): Direction? {
    return when {
        abs(dx) > abs(dy) && abs(dx) > SWIPE_THRESHOLD_DP ->
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        abs(dy) > abs(dx) && abs(dy) > SWIPE_THRESHOLD_DP ->
            if (dy > 0) Direction.DOWN else Direction.UP
        else -> null
    }
}

@Composable
fun SwipeableGameBoard(
    board: List<List<Int>>,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier,
    onNewGame: (() -> Unit)? = null,
    moveAnimationData: MoveAnimationData? = null
) {
    val focusRequester = remember { FocusRequester() }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var lastDirection by remember { mutableStateOf<Direction?>(null) }
    var hasSwiped by remember { mutableStateOf(false) }

    fun fire(direction: Direction) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        lastDirection = direction
        hasSwiped = true
        onSwipe(direction)
    }

    Box(
        modifier = modifier
            .shadow(24.dp, RoundedCornerShape(16.dp), spotColor = Color(0x55000000))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFC7B9AB), GameColors.BoardBackground)
                )
            )
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val direction = when (event.key) {
                        Key.DirectionUp, Key.W -> Direction.UP
                        Key.DirectionDown, Key.S -> Direction.DOWN
                        Key.DirectionLeft, Key.A -> Direction.LEFT
                        Key.DirectionRight, Key.D -> Direction.RIGHT
                        else -> null
                    }
                    if (direction != null) {
                        fire(direction)
                        true
                    } else if (event.key == Key.R && onNewGame != null) {
                        onNewGame()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .focusable()
            .pointerInput(Unit) {
                var startPosition = Offset.Zero
                var swiped = true
                detectDragGestures(
                    onDragStart = {
                        startPosition = it
                        swiped = false
                    },
                    onDragEnd = { },
                    onDragCancel = { },
                    onDrag = { change, _ ->
                        change.consume()
                        if (swiped) return@detectDragGestures
                        val dx = change.position.x - startPosition.x
                        val dy = change.position.y - startPosition.y
                        val direction = resolveSwipe(dx, dy)
                        if (direction != null) {
                            swiped = true
                            fire(direction)
                        }
                    }
                )
            }
            .padding(10.dp)
    ) {
        val boardSize = board.size

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (rowIdx in 0 until boardSize) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (colIdx in 0 until boardSize) {
                        key("cell-$rowIdx-$colIdx") {
                            AnimatedTile(
                                value = board[rowIdx][colIdx],
                                row = rowIdx,
                                col = colIdx,
                                boardSize = boardSize,
                                moveAnimationData = moveAnimationData,
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color(0x33000000))
                            )
                        }
                    }
                }
            }
        }

        if (!hasSwiped) {
            val infiniteTransition = rememberInfiniteTransition(label = "guide")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "guide-pulse"
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.board_swipe_to_play),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.HeaderText.copy(alpha = alpha * 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}