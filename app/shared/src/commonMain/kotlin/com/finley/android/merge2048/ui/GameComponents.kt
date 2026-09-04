package com.finley.android.merge2048.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
 * Reusable presentational building blocks of the Merge2048 design system.
 * These are pure UI components with no game-state awareness; they are wired
 * up by the screen layer ([com.finley.android.merge2048.GameScreen]).
 */
@Composable
fun ScoreBlock(
    label: String,
    value: Int,
    compact: Boolean = false
) {
    Column(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.ScoreBlockBackground)
            .padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 4.dp else 7.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.ScoreLabel
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(tween(200))) togetherWith
                    (fadeOut(tween(100)))
            },
            label = "score"
        ) { target ->
            Text(
                text = target.toString(),
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.width(5.dp))
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(tween(200)) togetherWith fadeOut(tween(100)))
            },
            label = "stat"
        ) { target ->
            Text(
                text = target,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
        }
    }
}

@Composable
fun OverlayStat(
    label: String,
    value: String,
    accent: Color = GameColors.SubText
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = GameColors.SubText
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = accent
        )
    }
}

@Composable
fun UndoButton(
    enabled: Boolean,
    undoCount: Int = 0,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground.copy(alpha = if (enabled) 1f else 0.4f),
            disabledContainerColor = GameColors.ButtonBackground.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = if (undoCount > 0) "\u21A9 UNDO \u00B7$undoCount" else "\u21A9 UNDO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}

@Composable
fun NewGameButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GameColors.ButtonBackground
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Text(
            text = "NEW GAME",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = Color.White
        )
    }
}

@Composable
fun GameOverlay(
    title: String,
    subtitle: String,
    score: Int,
    bestScore: Int,
    maxTile: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?,
    highlight: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCCFAF8EF))
            .clip(RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFFFFF))
                .padding(horizontal = 28.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.HeaderText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = GameColors.SubText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GameColors.ScoreBlockBackground)
                    .padding(horizontal = 32.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GameColors.ScoreLabel
                )
                Text(
                    text = score.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                OverlayStat(label = "BEST", value = bestScore.toString())
                OverlayStat(
                    label = "MAX",
                    value = if (maxTile == 0) "\u2014" else maxTile.toString(),
                    accent = GameColors.Tile2048
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = highlight
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = primaryLabel.uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.White
                )
            }

            if (secondaryLabel != null && onSecondary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSecondary
                ) {
                    Text(
                        text = secondaryLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameColors.SubText
                    )
                }
            }
        }
    }
}
