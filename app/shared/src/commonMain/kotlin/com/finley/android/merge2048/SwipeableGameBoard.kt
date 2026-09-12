package com.finley.android.merge2048

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.domain.Direction
import com.finley.android.merge2048.domain.MoveAnimationData
import com.finley.android.merge2048.domain.TileMovement
import merge2048.app.shared.generated.resources.Res
import merge2048.app.shared.generated.resources.board_swipe_to_play
import merge2048.app.shared.generated.resources.tile_content_desc_empty
import merge2048.app.shared.generated.resources.tile_content_desc_value
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val SWIPE_THRESHOLD_DP = 20f
private const val TILE_ANIM_DURATION_MS = 120
private const val SPAWN_ANIM_DURATION_MS = 200
private const val MERGE_POP_DURATION_MS = 200

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

/**
 * A single tile that animates from its previous position to its current position.
 *
 * KEY FIX: [key(moveId, movement) { ... }] wraps the entire animation block.
 * When the move changes, Compose discards the old subtree (including old Animatables)
 * and creates NEW ones with correct initial values. The moveId is unique per move, so
 * even two structurally identical moves (same merge into the same cell) restart the
 * animation. This eliminates the ghost that occurred because LaunchedEffect runs
 * asynchronously — the old Animatable value was rendered (or stale-reset) before the
 * effect could start.
 */
@Composable
private fun AnimatedTile(
    value: Int,
    row: Int,
    col: Int,
    boardSize: Int,
    moveAnimationData: MoveAnimationData?,
    modifier: Modifier = Modifier
) {
    val movement = remember(moveAnimationData, row, col) {
        moveAnimationData?.movements?.find { m ->
            when (m) {
                is TileMovement.Stayed -> m.row == row && m.col == col
                is TileMovement.Slid -> m.toRow == row && m.toCol == col
                is TileMovement.Merged -> m.toRow == row && m.toCol == col
                is TileMovement.Spawned -> m.row == row && m.col == col
            }
        }
    }

    // key(moveId, movement) forces full recomposition of this subtree whenever the
    // move changes. The moveId is monotonic, so even two structurally identical moves
    // (same merge into the same cell) count as different keys — otherwise Compose would
    // reuse the old settled Animatables and the tile would pop in place instead of
    // animating. Animatable initial values are computed from the movement synchronously,
    // so there is never a stale frame.
    key(moveAnimationData?.moveId ?: -1L, movement) {
        val offsetX = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Slid -> (movement.fromCol - col).toFloat()
                    is TileMovement.Merged -> (movement.from1Col - col).toFloat()
                    else -> 0f
                }
            )
        }
        val offsetY = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Slid -> (movement.fromRow - row).toFloat()
                    is TileMovement.Merged -> (movement.from1Row - row).toFloat()
                    else -> 0f
                }
            )
        }
        val mergeOffsetX = remember {
            Animatable(
                if (movement is TileMovement.Merged) (movement.from2Col - col).toFloat() else 0f
            )
        }
        val mergeOffsetY = remember {
            Animatable(
                if (movement is TileMovement.Merged) (movement.from2Row - row).toFloat() else 0f
            )
        }
        val mergeSecondAlpha = remember {
            Animatable(if (movement is TileMovement.Merged) 1f else 0f)
        }
        val scale = remember {
            Animatable(
                when (movement) {
                    is TileMovement.Spawned -> 0.3f
                    is TileMovement.Merged -> 0.8f
                    else -> 1f
                }
            )
        }

        LaunchedEffect(boardSize) {
            when (movement) {
                is TileMovement.Slid -> {
                    offsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                }
                is TileMovement.Merged -> {
                    offsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                    mergeOffsetX.animateTo(0f, tween(TILE_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                    mergeSecondAlpha.animateTo(0f, tween(80, easing = LinearEasing))
                    scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
                }
                is TileMovement.Spawned -> {
                    scale.animateTo(1f, tween(SPAWN_ANIM_DURATION_MS, easing = FastOutSlowInEasing))
                }
                is TileMovement.Stayed, null -> { }
            }
        }

        // --- Visual properties ---
        val tileDescEmpty = stringResource(Res.string.tile_content_desc_empty)
        val tileDescValue = stringResource(Res.string.tile_content_desc_value, 0)
        val isHigh = value >= 256
        val isMilestone = value >= 2048
        val glowColor = when {
            value >= 2048 -> GameColors.Tile2048
            value >= 1024 -> Color(0xFFEDC53F)
            value >= 512 -> Color(0xFFEDC850)
            value >= 256 -> Color(0xFFEDCC61)
            else -> Color(0x00000000)
        }

        // Only high tiles get the pulsing glow + scale animation. Empty cells and
        // ordinary tiles render fully static — an infinite transition per cell would
        // otherwise drive recomposition for the entire 6x6 board every frame.
        var glowAlpha = 0f
        var scalePulse = 1f
        if (isHigh) {
            val infiniteTransition = rememberInfiniteTransition(label = "tile-glow")
            glowAlpha = infiniteTransition.animateFloat(
                initialValue = if (isMilestone) 0.3f else 0.1f,
                targetValue = if (isMilestone) 0.7f else 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(if (isMilestone) 800 else 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow-pulse"
            ).value
            scalePulse = infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isMilestone) 1.02f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale-pulse"
            ).value
        }

        val glowModifier = if (isHigh) {
            modifier
                .shadow(if (isMilestone) 16.dp else 10.dp, RoundedCornerShape(8.dp), spotColor = glowColor.copy(alpha = glowAlpha))
                .fillMaxSize()
        } else {
            modifier.fillMaxSize()
        }

        // --- Main tile + optional ghost tile for merge animation ---
        // The background lives INSIDE the graphicsLayer (inner Box) so it
        // transforms together with the Text.  Previously the background was on
        // the outer Box while the Text was in the inner Box with graphicsLayer —
        // during any animation the background stayed put while the number moved
        // away or scaled down, making tiles appear as "background only, no number".
        Box(
            modifier = glowModifier
                .semantics {
                    contentDescription = if (value == 0) tileDescEmpty
                    else tileDescValue.replace("%1\$d", value.toString())
                },
            contentAlignment = Alignment.Center
        ) {
            // Main tile content — background + text slide/scale together.
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX.value * size.width
                        translationY = offsetY.value * size.height
                        scaleX = scale.value * scalePulse
                        scaleY = scale.value * scalePulse
                    }
                    .background(if (value == 0) GameColors.TileEmpty else tileBackgroundColor(value), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (value != 0) {
                    val cellFont = (maxWidth.value * tileFontFraction(value)).sp
                    Text(
                        text = value.toString(),
                        fontSize = cellFont,
                        fontWeight = FontWeight.Bold,
                        color = tileTextColor(value),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            // Ghost tile for merge animation — the second tile slides from its origin
            // to the merge point and fades out.  Its graphicsLayer is independent of
            // the main tile's so the two translations do NOT add up.
            if (movement is TileMovement.Merged && mergeSecondAlpha.value > 0f) {
                val halfValue = movement.value / 2
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = mergeOffsetX.value * size.width
                            translationY = mergeOffsetY.value * size.height
                            alpha = mergeSecondAlpha.value
                        }
                        .background(tileBackgroundColor(halfValue), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val cellFont = (maxWidth.value * tileFontFraction(halfValue)).sp
                    Text(
                        text = halfValue.toString(),
                        fontSize = cellFont,
                        fontWeight = FontWeight.Bold,
                        color = tileTextColor(halfValue),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
