package com.finley.android.merge2048

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.EnterTransition
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.Direction
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
    onNewGame: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var lastDirection by remember { mutableStateOf<Direction?>(null) }

    fun fire(direction: Direction) {
        lastDirection = direction
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
        AnimatedContent(
            targetState = board,
            transitionSpec = {
                val enter = directionSlideIn(lastDirection) + fadeIn()
                val exit = fadeOut(tween(120))
                (enter togetherWith exit).using(
                    SizeTransform(clip = false)
                )
            },
            label = "board-slide"
        ) { targetBoard ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in targetBoard) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (cell in row) {
                            GameTile(
                                value = cell,
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color(0x33000000))
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun directionSlideIn(direction: Direction?): EnterTransition {
    return when (direction) {
        Direction.LEFT -> slideInHorizontally(initialOffsetX = { -it / 8 }, animationSpec = tween(180))
        Direction.RIGHT -> slideInHorizontally(initialOffsetX = { it / 8 }, animationSpec = tween(180))
        Direction.UP -> slideInVertically(initialOffsetY = { -it / 8 }, animationSpec = tween(180))
        Direction.DOWN -> slideInVertically(initialOffsetY = { it / 8 }, animationSpec = tween(180))
        null -> fadeIn(tween(120))
    }
}

@Composable
fun GameTile(
    value: Int,
    modifier: Modifier = Modifier
) {
    val isHigh = value >= 256
    val glowColor = when {
        value >= 2048 -> GameColors.Tile2048
        value >= 1024 -> Color(0xFFEDC53F)
        value >= 512 -> Color(0xFFEDC850)
        value >= 256 -> Color(0xFFEDCC61)
        else -> Color(0x00000000)
    }

    val baseModifier = if (isHigh) {
        modifier
            .shadow(10.dp, RoundedCornerShape(8.dp), spotColor = glowColor.copy(alpha = 0.55f))
            .fillMaxSize()
    } else {
        modifier.fillMaxSize()
    }

    Box(
        modifier = baseModifier
            .clip(RoundedCornerShape(8.dp))
            .background(GameColors.TileEmpty),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState != 0 && initialState == 0) {
                    // New tile spawns with a pop
                    (scaleIn(initialScale = 0f, animationSpec = spring(dampingRatio = 0.6f)) togetherWith
                        scaleOut(targetScale = 0.6f, animationSpec = spring(dampingRatio = 0.6f)))
                } else if (targetState > initialState && initialState != 0) {
                    // Merge pop
                    (scaleIn(initialScale = 0.6f, animationSpec = spring(dampingRatio = 0.45f)) togetherWith
                        scaleOut(targetScale = 0.8f, animationSpec = spring(dampingRatio = 0.6f)))
                } else {
                    scaleIn(tween(150, easing = LinearEasing)) togetherWith
                        scaleOut(tween(150, easing = LinearEasing))
                }
            },
            label = "tile"
        ) { target ->
            if (target != 0) {
                Text(
                    text = target.toString(),
                    fontSize = tileFontSize(target).sp,
                    fontWeight = FontWeight.Bold,
                    color = tileTextColor(target),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}