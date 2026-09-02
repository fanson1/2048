package com.finley.android.merge2048

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFbbada0))
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
                            onSwipe(direction)
                        }
                    }
                )
            }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in board) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cell in row) {
                        GameTile(
                            value = cell,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameTile(
    value: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (value) {
        0 -> Color(0xFFcdc1b4)
        2 -> Color(0xFFeee4da)
        4 -> Color(0xFFede0c8)
        8 -> Color(0xFFf2b179)
        16 -> Color(0xFFf59563)
        32 -> Color(0xFFf67c5f)
        64 -> Color(0xFFf65e3b)
        128 -> Color(0xFFedcf72)
        256 -> Color(0xFFedcc61)
        512 -> Color(0xFFedc850)
        1024 -> Color(0xFFedc53f)
        2048 -> Color(0xFFedc22e)
        else -> Color(0xFF3c3a32)
    }

    val textColor = when (value) {
        0 -> Color.Transparent
        in listOf(2, 4) -> Color(0xFF776e65)
        else -> Color.White
    }

    val fontSize = when {
        value >= 1000 -> 20.sp
        value >= 100 -> 24.sp
        else -> 32.sp
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}