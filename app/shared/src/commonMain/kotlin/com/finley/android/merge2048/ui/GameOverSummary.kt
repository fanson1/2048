package com.finley.android.merge2048.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.merge2048.GameColors

/**
 * Enhanced game-over summary modal showing detailed session stats.
 * Includes: score, max tile, moves, merges, average merge value,
 * and encouragement message based on performance.
 */
@Composable
fun GameOverSummary(
    visible: Boolean,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    moveCount: Int,
    totalMerges: Int,
    isNewBest: Boolean,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.9f, animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clip(RoundedCornerShape(16.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1A2E))
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (isNewBest) "NEW RECORD!" else "GAME OVER",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isNewBest) GameColors.Tile2048 else Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = getEncouragementMessage(maxTile, score),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Score card
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF252540))
                        .padding(horizontal = 36.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = score.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStat(label = "BEST", value = bestScore.toString())
                    SummaryStat(label = "MAX", value = maxTile.toString(), accent = GameColors.Tile2048)
                    SummaryStat(label = "MOVES", value = moveCount.toString())
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStat(
                        label = "MERGES",
                        value = totalMerges.toString()
                    )
                    SummaryStat(
                        label = "AVG MERGE",
                        value = if (totalMerges > 0) (score / totalMerges).toString() else "0"
                    )
                    SummaryStat(
                        label = "EFFICIENCY",
                        value = if (moveCount > 0) "${(totalMerges * 100 / moveCount)}%" else "0%"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Button(
                    onClick = onNewGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.ButtonBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "CLOSE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    accent: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White.copy(alpha = 0.4f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}

private fun getEncouragementMessage(maxTile: Int, score: Int): String {
    return when {
        maxTile >= 4096 -> "Legendary performance! You are a master!"
        maxTile >= 2048 -> "Incredible! The 2048 tile is no small feat!"
        maxTile >= 1024 -> "Great game! You're getting close to 2048!"
        maxTile >= 512 -> "Nice run! Keep pushing for bigger tiles!"
        maxTile >= 256 -> "Good effort! Every merge counts!"
        score >= 5000 -> "Strong score! Try for a higher max tile next time!"
        score >= 2000 -> "Decent game! Focus on building up large tiles."
        else -> "Every game teaches you something new. Try again!"
    }
}
