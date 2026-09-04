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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors

/**
 * A score label that fades in and floats upward when [points] is non-zero,
 * then disappears. Re-triggers each time [points] changes to a non-zero value.
 */
@Composable
fun FloatingScore(
    points: Int,
    modifier: Modifier = Modifier
) {
    // Re-key on points so the animation restarts each time.
    if (points <= 0) return

    val transition = updateTransition(points, label = "float")

    val offsetY by transition.animateInt(
        label = "offset",
        transitionSpec = {
            tween(1200, easing = FastOutSlowInEasing)
        }
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