package com.finley.android.merge2048.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors

/**
 * Shows progress toward the next power-of-2 tile. The progress is calculated
 * as how far the current max tile is between the previous and next power-of-2.
 *
 * Example: if maxTile is 48, progress = (48 - 32) / (64 - 32) = 50%
 */
@Composable
fun TileProgressBar(
    maxTile: Int,
    modifier: Modifier = Modifier
) {
    if (maxTile <= 0) return

    val nextPower = nextPowerOfTwo(maxTile)
    val prevPower = prevPowerOfTwo(maxTile)

    val progress = if (nextPower == prevPower) {
        1f
    } else {
        ((maxTile - prevPower).toFloat() / (nextPower - prevPower)).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Subtle shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prevPower.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.SubText
            )
            Text(
                text = "Next: $nextPower",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.SubText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .shadow(1.dp, RoundedCornerShape(3.dp))
                .clip(RoundedCornerShape(3.dp))
                .background(GameColors.ScoreBlockBackground)
        ) {
            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GameColors.ButtonBackground,
                                GameColors.Tile2048
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.SubText,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

private fun nextPowerOfTwo(n: Int): Int {
    var p = 1
    while (p < n) p *= 2
    return p
}

private fun prevPowerOfTwo(n: Int): Int {
    var p = 1
    while (p * 2 <= n) p *= 2
    return p
}
